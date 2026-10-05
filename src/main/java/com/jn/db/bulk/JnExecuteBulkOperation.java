package com.jn.db.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkExecutor;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.utils.JnDeleteKeysFromCache;
import java.util.stream.Stream;

/**
 * Orchestrates bulk operations on the JobsNow Elasticsearch. Sanitizes duplicated items
 * by solving conflicts by priority, executes the bulk via CcpBulkExecutor, processes the
 * error results by recursively creating reprocessing records, and invalidates the cache
 * keys of the items that were processed successfully.
 */
public class JnExecuteBulkOperation implements CcpExecuteBulkOperation{

	/** The single instance. */
	public static final JnExecuteBulkOperation INSTANCE = new JnExecuteBulkOperation();

	/** Singleton; use {@link #INSTANCE}. */
	private JnExecuteBulkOperation() {}

	/**
	 * Sanitizes the items, runs the bulk, turns the failures into records to reprocess (in another bulk) and deletes the
	 * cache keys of the items that succeeded.
	 * @param bulkItems the items
	 * @param functionToDeleteKeysInTheCache the cache cleanup
	 * @return this instance
	 */
	public JnExecuteBulkOperation executeBulk(Collection<CcpBulkItem> bulkItems,  Consumer<String[]> functionToDeleteKeysInTheCache) {
		
		HashSet<CcpBulkItem> items = this.sanitizeItems(bulkItems);
		
		boolean emptyItems = items.isEmpty(); 
		 
		if(emptyItems) {
			return this;
		}

		CcpBulkExecutor dbBulkExecutor = CcpDependencyInjection.getDependency(CcpBulkExecutor.class);
		
		for (CcpBulkItem item : items) {
			dbBulkExecutor = dbBulkExecutor.addRecord(item);
		}
 		JnExecuteBulkOperation result = this.commitAndSaveErrorsAndDeleteRecordsFromCache(dbBulkExecutor, functionToDeleteKeysInTheCache);
		return result;
	}

	/**
	 * Removes the items with no priority and keeps a single item per document: between two items of the same document the
	 * one whose operation has the higher priority wins, and on a tie the later one.
	 * @param bulkItems the items
	 * @return the sanitized items
	 */
	private HashSet<CcpBulkItem> sanitizeItems(Collection<CcpBulkItem> bulkItems) {
		HashSet<CcpBulkItem> items = new HashSet<>();
		
		for (CcpBulkItem newerItem : bulkItems) {
			boolean hasNoPriority = newerItem.operation.priority <= 0;
		
			if(hasNoPriority) {
				continue;
			}
			boolean alreadyAdded = items.contains(newerItem);

			boolean isNewItem = false == alreadyAdded;
			
			if(isNewItem) {
				items.add(newerItem);
				continue;
			}
			
			ArrayList<CcpBulkItem> itemsList = new ArrayList<>(items);
			int olderItemIndex = itemsList.indexOf(newerItem);
			CcpBulkItem olderItem = itemsList.get(olderItemIndex);
			int priorityDifference = olderItem.operation.priority - newerItem.operation.priority;

			boolean doesNotOverride = (priorityDifference) > 0;
			
			if(doesNotOverride) {
				continue;
			}
			items.remove(olderItem);
			items.add(newerItem);
		}
		return items;
	}
	
	/**
	 * Runs the bulk, sends the failures to reprocessing and cleans the cache.
	 * @param dbBulkExecutor the bulk with the items
	 * @param functionToDeleteKeysInTheCache the cache cleanup
	 * @return this instance
	 */
	private JnExecuteBulkOperation commitAndSaveErrorsAndDeleteRecordsFromCache(CcpBulkExecutor dbBulkExecutor, Consumer<String[]> functionToDeleteKeysInTheCache) {

		List<CcpBulkOperationResult> allResults = dbBulkExecutor.getBulkOperationResult();
		Stream<CcpBulkOperationResult> allResultsStream = allResults.stream();
		var failedResultsStream = allResultsStream.filter(x -> x.hasError());
		List<CcpBulkOperationResult> errors = failedResultsStream.collect(Collectors.toList());
		Stream<CcpBulkOperationResult> errorsStream = errors.stream();
		var reprocessItemsStream = errorsStream.map(x -> x.getReprocess(FunctionReprocessMapper.INSTANCE, JnEntityRecordToReprocess.ENTITY));
		List<CcpBulkItem> reprocessItems = reprocessItemsStream.collect(Collectors.toList());
		this.executeBulk(reprocessItems, functionToDeleteKeysInTheCache);
		JnExecuteBulkOperation resultAfterCacheCleanup = this.deleteKeysFromCache(allResults);
		return resultAfterCacheCleanup; 
	}

	/**
	 * Deletes from the cache the keys of the items that succeeded.
	 * @param allResults the bulk results
	 * @return this instance
	 */
	private JnExecuteBulkOperation deleteKeysFromCache(List<CcpBulkOperationResult> allResults) {
		var resultsStream = new ArrayList<>(allResults).stream();
		var successfulResultsStream = resultsStream
		.filter(x -> false == x.hasError());
		var cacheKeysStream = successfulResultsStream
		.map(x -> x.getCacheKey());
		Set<String> keysToDeleteInCache = cacheKeysStream
		.collect(Collectors.toSet());
		int keysToDeleteInCacheSize = keysToDeleteInCache.size();
		String[] keysToDelete = keysToDeleteInCache.toArray(new String[keysToDeleteInCacheSize]);
		
		JnDeleteKeysFromCache.INSTANCE.accept(keysToDelete);
		return this;
	}
	
	/**
	 * Creates each JSON as a new record of the entity in a single bulk, without turning a conflict into an update. Returns
	 * one {@link JnBulkCreateResult} per JSON, in the same order, pairing the JSON with whether every record derived from it
	 * was created (status 201). A JSON comes back with {@code created = false}, and is not written, when its record already
	 * exists in any of {@code entitiesThatPreventCreation} (checked in a single union all before the bulk) or in the entity
	 * itself (status 409). Any other failure goes to the usual reprocessing and also comes back with
	 * {@code created = false}.
	 * @param entity the entity
	 * @param entitiesThatPreventCreation entities where an existing record prevents the creation
	 * @param functionToDeleteKeysInTheCache the cache cleanup
	 * @param jsons the records
	 * @return the outcome of each JSON
	 */
	public List<JnBulkCreateResult> executeCreateBulk(CcpEntity entity, CcpEntity[] entitiesThatPreventCreation, Consumer<String[]> functionToDeleteKeysInTheCache, CcpJsonRepresentation... jsons) {

		boolean noJsons = jsons.length == 0;

		if(noJsons) {
			return new ArrayList<>();
		}

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(jsons, functionToDeleteKeysInTheCache, entitiesThatPreventCreation);

		CcpBulkExecutor dbBulkExecutor = CcpDependencyInjection.getDependency(CcpBulkExecutor.class);
		List<List<CcpBulkItem>> itemsByJson = new ArrayList<>();

		for (CcpJsonRepresentation json : jsons) {
			Stream<CcpEntity> entitiesThatPreventCreationStream = Arrays.stream(entitiesThatPreventCreation);
			boolean isPreventedFromBeingCreated = entitiesThatPreventCreationStream.anyMatch(x -> x.isPresentInThisUnionAll(unionAll, json));

			if(isPreventedFromBeingCreated) {
				List<CcpBulkItem> noItems = new ArrayList<>();
				itemsByJson.add(noItems);
				continue;
			}

			List<CcpBulkItem> jsonItems = entity.toBulkItems(json, CcpBulkEntityOperationType.create);
			itemsByJson.add(jsonItems);
			dbBulkExecutor = dbBulkExecutor.addRecords(jsonItems);
		}

		List<CcpBulkOperationResult> allResults = dbBulkExecutor.getBulkOperationResult();

		int conflictStatus = CcpProcessStatusDefault.CONFLICT.asNumber();
		Stream<CcpBulkOperationResult> allResultsStream = allResults.stream();
		var failedResultsStream = allResultsStream.filter(x -> x.hasError() && x.status() != conflictStatus);
		var reprocessItemsStream = failedResultsStream.map(x -> x.getReprocess(FunctionReprocessMapper.INSTANCE, JnEntityRecordToReprocess.ENTITY));
		List<CcpBulkItem> reprocessItems = reprocessItemsStream.collect(Collectors.toList());
		this.executeBulk(reprocessItems, functionToDeleteKeysInTheCache);
		this.deleteKeysFromCache(allResults);

		int createdStatus = CcpProcessStatusDefault.CREATED.asNumber();
		Stream<CcpBulkOperationResult> resultsStream = allResults.stream();
		var createdResultsStream = resultsStream.filter(x -> x.status() == createdStatus);
		var createdItemsStream = createdResultsStream.map(x -> x.getBulkItem());
		Set<CcpBulkItem> createdItems = createdItemsStream.collect(Collectors.toSet());

		List<JnBulkCreateResult> resultByJson = new ArrayList<>();

		for (int index = 0; index < jsons.length; index++) {
			List<CcpBulkItem> jsonItems = itemsByJson.get(index);
			boolean hasItems = false == jsonItems.isEmpty();
			boolean allItemsCreated = createdItems.containsAll(jsonItems);
			boolean created = hasItems && allItemsCreated;
			JnBulkCreateResult jsonResult = new JnBulkCreateResult(jsons[index], created);
			resultByJson.add(jsonResult);
		}

		return resultByJson;
	}

	/**
	 * Converts the JSON into the bulk items of each entity for the operation and runs them as one bulk.
	 * @param json the record
	 * @param operation the bulk operation
	 * @param functionToDeleteKeysInTheCache the cache cleanup
	 * @param entities the entities
	 * @return this instance
	 */
	public JnExecuteBulkOperation executeBulk(CcpJsonRepresentation json, CcpBulkEntityOperationType operation,  Consumer<String[]> functionToDeleteKeysInTheCache, CcpEntity...entities) {
		
		List<CcpBulkItem> items = new ArrayList<>();
 		
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			for (CcpBulkItem bulkItem : bulkItems) {
				items.add(bulkItem);
			}			
		}
		
		JnExecuteBulkOperation result = this.executeBulk(items, functionToDeleteKeysInTheCache);
		return result;
	}
}
