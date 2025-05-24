package com.jn.db.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.bulk.CcpDbBulkExecutor;
import com.ccp.especifications.db.bulk.CcpEntityBulkOperationType;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSave;
import com.ccp.especifications.db.bulk.handlers.CcpBulkHandlerSaveTwin;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpEntity;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.utils.JnDeleteKeysFromCache;


public class JnExecuteBulkOperation implements CcpExecuteBulkOperation{

	public static final JnExecuteBulkOperation INSTANCE = new JnExecuteBulkOperation();
	
	private JnExecuteBulkOperation() {}
	
	public JnExecuteBulkOperation executeBulk(List<CcpJsonRepresentation> records, CcpEntityBulkOperationType operation, CcpEntity entity) {
		
		boolean emptyRecords = records.isEmpty();
		
		if(emptyRecords) {
			return this;
		}
		
		List<CcpBulkItem> collect = records.stream().map(json -> entity.getMainBulkItem(json, operation)).collect(Collectors.toList());
		
		JnExecuteBulkOperation executeBulk = this.executeBulk(collect);
		return executeBulk;
	}
	
	public JnExecuteBulkOperation executeBulk(CcpJsonRepresentation json, CcpEntity entity, CcpEntityBulkOperationType operation) {
		CcpEntity twinEntity = entity.getTwinEntity();
		CcpBulkItem bulkItem = entity.getMainBulkItem(json, operation);
		CcpBulkItem bulkItem2 = twinEntity.getMainBulkItem(json, operation);
		JnExecuteBulkOperation executeBulk = this.executeBulk(bulkItem, bulkItem2);
		return executeBulk;
	}
	
	public JnExecuteBulkOperation executeBulk(CcpBulkItem... items) {
		List<CcpBulkItem> asList = Arrays.asList(items);
		JnExecuteBulkOperation executeBulk = this.executeBulk(asList);
		return executeBulk;
	}
	
	public JnExecuteBulkOperation executeBulk(Collection<CcpBulkItem> items) {
		
		boolean emptyItems = items.isEmpty();
		
		if(emptyItems) {
			return this;
		}

		CcpDbBulkExecutor dbBulkExecutor = CcpDependencyInjection.getDependency(CcpDbBulkExecutor.class);
		
		for (CcpBulkItem item : items) {
			dbBulkExecutor = dbBulkExecutor.addRecord(item);
		}
 		JnExecuteBulkOperation commitAndSaveErrorsAndDeleteRecordsFromCache = this.commitAndSaveErrorsAndDeleteRecordsFromCache(dbBulkExecutor);
		return commitAndSaveErrorsAndDeleteRecordsFromCache;
	}
	
	private JnExecuteBulkOperation commitAndSaveErrorsAndDeleteRecordsFromCache(CcpDbBulkExecutor dbBulkExecutor) {

		List<CcpBulkOperationResult> allResults = dbBulkExecutor.getBulkOperationResult();
		List<CcpBulkOperationResult> errors = allResults.stream().filter(x -> x.hasError()).collect(Collectors.toList());
		List<CcpBulkItem> collect = errors.stream().map(x -> x.getReprocess(ReprocessMapper.INSTANCE, JnEntityRecordToReprocess.ENTITY)).collect(Collectors.toList());
		this.executeBulk(collect);
		JnExecuteBulkOperation deleteKeysFromCache = this.deleteKeysFromCache(allResults);
		return deleteKeysFromCache; 
	}

	private JnExecuteBulkOperation deleteKeysFromCache(List<CcpBulkOperationResult> allResults) {
		Set<String> keysToDeleteInCache = new ArrayList<>(allResults).stream()
		.filter(x -> x.hasError() == false)
		.map(x -> x.getCacheKey())
		.collect(Collectors.toSet());
		String[] array = keysToDeleteInCache.toArray(new String[keysToDeleteInCache.size()]);
		
		JnDeleteKeysFromCache.INSTANCE.accept(array);
		return this;
	}
	
	public CcpSelectUnionAll changeStatus(CcpJsonRepresentation json, CcpEntity entity) {
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionBetweenMainAndTwinEntities(json, JnDeleteKeysFromCache.INSTANCE, entity);
		CcpEntity twinEntity = entity.getTwinEntity();
		CcpBulkItem twin = twinEntity.toBulkItemToCreateOrDelete(unionAll, json);
		CcpBulkItem main = entity.toBulkItemToCreateOrDelete(unionAll, json);
		
		this.executeBulk(main, twin);
		return unionAll;
	}
	
	@SuppressWarnings("unchecked")
	public CcpSelectUnionAll executeSelectUnionAllThenExecuteBulkOperation(CcpJsonRepresentation json,  Consumer<String[]> functionToDeleteKeysInTheCache, CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>> ... handlers) {
		Set<CcpEntity> collect = Arrays.asList(handlers).stream().map(x -> x.getEntityToSearch()).collect(Collectors.toSet());
		CcpEntity[] array = collect.toArray(new CcpEntity[collect.size()]);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(json, functionToDeleteKeysInTheCache, array);
		
		Set<CcpBulkItem> all = new HashSet<>();
		
		for (CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>> handler : handlers) {
			List<CcpBulkItem> list =  unionAll.handleRecordInUnionAll(json, handler);
			all.addAll(list);
		}
		this.executeBulk(all);

		CcpJsonRepresentation data = json;
	
		for (CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>> handler : handlers) {
			
			CcpEntity entityToSearch = handler.getEntityToSearch();
			
			boolean presentInThisUnionAll = entityToSearch.isPresentInThisUnionAll(unionAll, data);
			
			if(presentInThisUnionAll) {
				List<Function<CcpJsonRepresentation, CcpJsonRepresentation>> doAfterSavingIfRecordIsFound = handler.doAfterSavingIfRecordIsFound();
				data = data.getTransformedJson(doAfterSavingIfRecordIsFound);
				continue;
			}
			List<Function<CcpJsonRepresentation, CcpJsonRepresentation>> doAfterSavingIfRecordIsNotFound = handler.doAfterSavingIfRecordIsNotFound();
			data = data.getTransformedJson(doAfterSavingIfRecordIsNotFound);
		}
		
		return unionAll;
	}

	@SuppressWarnings("unchecked")
	public CcpJsonRepresentation executeSelectUnionAllThenSaveInTheMainAndTwinEntities(CcpJsonRepresentation json, 
			CcpEntity mainEntity, Function<CcpJsonRepresentation, CcpJsonRepresentation> whenPresentInMainEntityOrIsNewRecord) {
		
		CcpEntity supportEntity = mainEntity.getTwinEntity();
		
		CcpBulkHandlerSave saveMainEntity = new CcpBulkHandlerSave(mainEntity);
		
		CcpBulkHandlerSaveTwin saveSupportEntity = new CcpBulkHandlerSaveTwin(supportEntity);
		
		CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>[] array = new CcpHandleWithSearchResultsInTheEntity[]{
				saveMainEntity,
				saveSupportEntity
		};
		
		CcpSelectUnionAll result = this.executeSelectUnionAllThenExecuteBulkOperation(json, JnDeleteKeysFromCache.INSTANCE, array);
		
		boolean isPresentInMainEntity = mainEntity.isPresentInThisUnionAll(result, json);
		
		if(isPresentInMainEntity) {
			CcpJsonRepresentation apply = whenPresentInMainEntityOrIsNewRecord.apply(json);
			return apply;
		}
		
		boolean isNewRecord = false == supportEntity.isPresentInThisUnionAll(result, json);

		if(isNewRecord) {
			CcpJsonRepresentation apply = whenPresentInMainEntityOrIsNewRecord.apply(json);
			return apply;
		}
			
		return json;
	}

	public JnExecuteBulkOperation executeBulk(CcpJsonRepresentation json, CcpEntityBulkOperationType operation, CcpEntity...entities) {
		
		List<CcpBulkItem> items = new ArrayList<>();
 		
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			for (CcpBulkItem bulkItem : bulkItems) {
				items.add(bulkItem);
			}			
		}
		
		JnExecuteBulkOperation executeBulk = this.executeBulk(items);
		return executeBulk;
	}
}
