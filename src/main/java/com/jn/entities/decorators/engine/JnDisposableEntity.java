package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Expiration decorator ({@code @JnEntityDisposable}). The id of a record is the SHA-1 of the current period (formatted by
 * the granularity) plus the original id, so each period has its own document; every write also writes, in
 * {@code jn_disposable_record}, a copy of the record keyed by entity and primary key with the expiration timestamp
 * (see finding: the expiration is computed from the current time). A record counts as present when the document of the
 * current period exists, or when the copy exists and has not expired.
 */
public class JnDisposableEntity extends CcpDefaultEntityDelegator<Object>{
	
	/** The expiration granularity. */
	private final CcpEntityExpurgableOptions timeOption;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param timeOption the expiration granularity
	 */
	public JnDisposableEntity(CcpEntity entity, CcpEntityExpurgableOptions timeOption) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
		this.timeOption = timeOption;
	}
	
	/**
	 * Returns the key of the copy: {@code entity} (name) and {@code id} (the primary key values as compact JSON).
	 * @param json the record
	 * @return the key of the copy
	 */
	private CcpJsonRepresentation getExpurgableId(CcpJsonRepresentation json) {
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();

		Supplier<CcpJsonRepresentation> supplier = json.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(supplier);
		String id = primaryKeyValues.asUgglyJson();
		CcpJsonRepresentation jsonWithEntityName = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);

				CcpJsonRepresentation expurgableId = jsonWithEntityName
				.put(JnJsonCommonsFields.id, id)
				;
		return expurgableId;
	}
	
	/**
	 * Formats the current time by the granularity.
	 * @return the current period
	 */
	private String extractFormatedCurrentTimestamp() {
		long currentTimeMillis = System.currentTimeMillis();
		String formattedTimestamp = this.timeOption.getFormattedDate(currentTimeMillis);
		return formattedTimestamp;
	}
	
	
	/**
	 * Tells whether the copy has a {@code timestamp} in the future.
	 * @param requiredEntityRow the copy
	 * @return {@code true} for a copy not expired yet
	 */
	private boolean isValidTimestamp(CcpJsonRepresentation requiredEntityRow) {
		
		String timeStampFieldName = JnJsonCommonsFields.timestamp.name();
		CcpFieldName timestampField = new CcpFieldName(timeStampFieldName);
		boolean containsAllFields = requiredEntityRow.containsAllFields(timestampField);

		boolean recordNotFound = false == containsAllFields;

		if(recordNotFound) {
			return false;
		}
		CcpFieldName timestampFieldToRead = new CcpFieldName(timeStampFieldName);

		Long timeStamp = requiredEntityRow.getAsLongNumber(timestampFieldToRead);
		long now = System.currentTimeMillis();
		boolean isInTheFuture = timeStamp > now;

		if(isInTheFuture) {
			return true;
		}
		return false;
	}
	/**
	 * Builds the bulk item of the copy in {@code jn_disposable_record}.
	 * @param json the record
	 * @param operation the bulk operation
	 * @return the bulk item of the copy
	 */
	private final CcpBulkItem getExpurgableToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation recordCopy = this.populateAnExpurgableFromJson(json);
		String disposableRecordId = JnEntityDisposableRecord.ENTITY.calculateId(recordCopy);
		CcpBulkItem disposableBulkItem = new CcpBulkItem(recordCopy, operation, JnEntityDisposableRecord.ENTITY, disposableRecordId);
		
		return disposableBulkItem;
	}
	
	/**
	 * Builds the copy: key, {@code format}, {@code timestamp} (expiration), {@code trueTimestamp} (when it was written),
	 * {@code json} (the record fields), {@code date} (expiration date) and {@code id}.
	 * @param json the record
	 * @return the copy
	 */
	private CcpJsonRepresentation populateAnExpurgableFromJson(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(jsonSupplier);
		String id = primaryKeyValues.asUgglyJson();
		Long timestamp = json.getOrDefault(JnJsonCommonsFields.timestamp, () -> System.currentTimeMillis());
		CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(json);
		Long nextTimeStamp = this.timeOption.getNextTimeStamp(timestamp);
		String nextDate = this.timeOption.getNextDate(timestamp);
		CcpJsonRepresentation jsonWithFormat = expurgableId
				.put(JnEntityDisposableRecord.Fields.format, this.timeOption.format);
				CcpJsonRepresentation jsonWithNextTimestamp = jsonWithFormat
				.put(JnJsonCommonsFields.timestamp, nextTimeStamp);
				CcpJsonRepresentation jsonWithTrueTimestamp = jsonWithNextTimestamp
				.put(JnEntityDisposableRecord.Fields.trueTimestamp, timestamp);
				CcpJsonRepresentation jsonWithOriginalJson = jsonWithTrueTimestamp
				.put(JnJsonCommonsFields.json,onlyExistingFields);
				CcpJsonRepresentation jsonWithDate = jsonWithOriginalJson
				.put(JnJsonCommonsFields.date, nextDate);

				CcpJsonRepresentation expurgable = jsonWithDate
				.put(JnJsonCommonsFields.id, id)
				;
		return expurgable;
	}

	/**
	 * Computes the id of the document of the current period: SHA-1 of the formatted period and the original id.
	 * @param json the record
	 * @return the id
	 */
	public String calculateId(CcpJsonRepresentation json) {

		String formattedTimestamp = this.extractFormatedCurrentTimestamp();
		String entityId = this.entity.calculateId(json);

		ArrayList<Object> onlyPrimaryKeysValues = new ArrayList<>();
		onlyPrimaryKeysValues.add(formattedTimestamp);
		onlyPrimaryKeysValues.add(entityId);
		String primaryKeysAsString = onlyPrimaryKeysValues.toString();
		String withoutOpeningBracket = primaryKeysAsString.replace("[", "");

		String joinedPrimaryKeys = withoutOpeningBracket.replace("]", "");
		CcpStringDecorator joinedPrimaryKeysDecorator = new CcpStringDecorator(joinedPrimaryKeys);
		CcpHashDecorator hashDecorator = joinedPrimaryKeysDecorator.hash();
		String hash = hashDecorator.asString(CcpHashAlgorithm.SHA1);
		return hash;
	}

	/**
	 * Tells whether the record exists in the current period, or has a copy not expired yet.
	 * @param json the record
	 * @return {@code true} when the record is valid
	 */
	public boolean exists(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		CcpSelectUnionAll unionAll = crud.unionAll(allValuesTogether, JnDeleteKeysFromCache.INSTANCE, this, JnEntityDisposableRecord.ENTITY);

		boolean isPresentInOriginalEntity = this.isPresentInThisUnionAll(unionAll, allValuesTogether);
		
		if(isPresentInOriginalEntity) {
			return true;
		}
		boolean isPresentInDisposable = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);

		boolean isNotPresentInCopyEntity = false == isPresentInDisposable;
		
		if(isNotPresentInCopyEntity) {
			return false;
		}
		
		Supplier<CcpJsonRepresentation> jsonSupplier = expurgableId.getJsonSupplier();
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, jsonSupplier);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnJsonCommonsFields.timestamp);
		long now = System.currentTimeMillis();

		boolean obsoleteTimeStamp = timeStamp <= now;
		
		if(obsoleteTimeStamp) {
			return false;
		}
		return true;
	}
	
	/**
	 * The associated entities of the wrapped entity plus {@code jn_disposable_record}.
	 * @return the associated entities
	 */
	public List<CcpEntity> getAssociatedEntities() {
		List<CcpEntity> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity> result = new ArrayList<CcpEntity>(associatedEntities);
		result.add(JnEntityDisposableRecord.ENTITY);
		return result;
	}
	
	/**
	 * Returns the document of the current period; otherwise the record kept in a copy not expired yet; otherwise
	 * delegates inward (which raises the not-found error).
	 * @param json the record
	 * @return the record
	 */
	public CcpJsonRepresentation getOneById(CcpJsonRepresentation json) {
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		CcpSelectUnionAll unionAll = crud.unionAll(allValuesTogether, JnDeleteKeysFromCache.INSTANCE, this, JnEntityDisposableRecord.ENTITY);

		boolean isPresentInOriginalEntity = this.isPresentInThisUnionAll(unionAll, allValuesTogether);
		
		if(isPresentInOriginalEntity) {
			CcpJsonRepresentation requiredEntityRow = this.getRecordFromUnionAll(unionAll, allValuesTogether);
			return requiredEntityRow;
		}
		boolean isPresentInDisposable = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, allValuesTogether);

		boolean isNotPresentInCopyEntity = false == isPresentInDisposable;

		if(isNotPresentInCopyEntity) {
			CcpJsonRepresentation oneById =  this.entity.getOneById(json);
			return oneById;
		}

		Supplier<CcpJsonRepresentation> jsonSupplier = allValuesTogether.getJsonSupplier();
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, jsonSupplier);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnJsonCommonsFields.timestamp);
		long now = System.currentTimeMillis();

		boolean validTimeStamp = timeStamp > now;
		
		if(validTimeStamp) {
			CcpJsonRepresentation innerJson = requiredEntityRow.getInnerJson(JnJsonCommonsFields.json);
			return innerJson;
		}

		CcpJsonRepresentation oneById =  this.entity.getOneById(json);
		return oneById;
	}
	
	/**
	 * Searches every associated entity, the copy included.
	 * @param json the record
	 * @return the records by entity
	 */
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		
		CcpJsonRepresentation result = super.getOneByIdAnyWhere(allValuesTogether);
		return result;
	}
	
	/**
	 * Replaces the id of the search parameter of this entity with the id of the current period.
	 * @param parameterToSearch the search parameter
	 * @param json the record
	 * @return the adjusted parameter
	 */
	private CcpJsonRepresentation replaceParameterToSearch(CcpJsonRepresentation parameterToSearch, CcpJsonRepresentation json) {

		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		String fieldNameToEntity = dbRequester.getFieldNameToEntity();
		CcpFieldName entityField = new CcpFieldName(fieldNameToEntity);

		String entityName = parameterToSearch.getAsString(entityField);
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		boolean entityNameEquals = entityName.equals(entityDetails.entityName);
		boolean isAnotherEntity = false == entityNameEquals;
	
		if(isAnotherEntity) {
			return parameterToSearch;
		}
		
		String fieldNameToId = dbRequester.getFieldNameToId();
		String id = this.calculateId(json);
		CcpFieldName idField = new CcpFieldName(fieldNameToId);
		CcpJsonRepresentation parameterWithCalculatedId = parameterToSearch.put(idField, id);
		return parameterWithCalculatedId;
	}
	
	/**
	 * The search parameters of the copy plus the ones of the entity with the id of the current period.
	 * @param json the record
	 * @return the search parameters
	 */
	public List<CcpJsonRepresentation> getParametersToSearch(CcpJsonRepresentation json) {
		List<CcpJsonRepresentation> parametersToSearch = this.entity.getParametersToSearch(json);
		Stream<CcpJsonRepresentation> parametersStream = parametersToSearch
				.stream();
				var replacedParametersStream = parametersStream
				.map(p -> this.replaceParameterToSearch(p, json));

				List<CcpJsonRepresentation> mainParametersToSearch =  replacedParametersStream
				.collect(Collectors.toList())
				;
		
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		List<CcpJsonRepresentation> othersParametersToSearch = JnEntityDisposableRecord.ENTITY.getParametersToSearch(expurgableId);
		ArrayList<CcpJsonRepresentation> result = new ArrayList<>();
		result.addAll(othersParametersToSearch);
		result.addAll(mainParametersToSearch);
		return result;
	}

	/**
	 * Returns the document of the current period from the search result; otherwise the record kept in a copy not expired
	 * yet; otherwise an empty JSON.
	 * @param unionAll the search result
	 * @param json the record
	 * @return the record
	 */
	public CcpJsonRepresentation getRecordFromUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json) {

		String id = this.calculateId(json);
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		CcpJsonRepresentation recordFromUnionAll = unionAll.getEntityRow(entityDetails.entityName, id);
		boolean recordFromUnionAllEmpty = recordFromUnionAll.isEmpty();

		boolean recordFound = false == recordFromUnionAllEmpty;
		
		if(recordFound) {
			return recordFromUnionAll;
		}

		Supplier<CcpJsonRepresentation> jsonSupplier = () -> this.getExpurgableId(json);
		CcpJsonRepresentation recordFromDisposable = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, jsonSupplier);
		boolean validTimestamp = this.isValidTimestamp(recordFromDisposable);

		boolean isInvalid = false == validTimestamp;
	
		if(isInvalid) {
			return CcpOtherConstants.EMPTY_JSON;
		}
		
		CcpJsonRepresentation innerJson = recordFromDisposable.getInnerJson(JnJsonCommonsFields.json);
		return innerJson;
	}
	
	/**
	 * Tells whether the search found the document of the current period or a copy not expired yet.
	 * @param unionAll the search result
	 * @param json the record
	 * @return {@code true} when the record is valid
	 */
	public boolean isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json) {

		String id = this.calculateId(json);

		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		boolean presentInThisUnionAll = unionAll.isPresent(entityDetails.entityName, id);

		if(presentInThisUnionAll) {
			return true;
		}
		
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		boolean isPresentInDisposable = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);

		boolean notFoundInDisposable = false == isPresentInDisposable;
		
		if(notFoundInDisposable) {
			return false;
		}
		
		CcpJsonRepresentation expurgableIdWithJson = expurgableId.mergeWithAnotherJson(json);
		
		CcpJsonRepresentation requiredEntityRow = this.getRecordFromUnionAll(unionAll, expurgableIdWithJson);
		
		boolean valid = this.isValidTimestamp(requiredEntityRow);
		
		if(valid) {
			return true;
		}
		return false;
	}

	/**
	 * Gives the bulk item of this entity (or of its twin) the id of the current period; items of other entities are kept.
	 * @param item the bulk item
	 * @return the adjusted item
	 */
	private CcpBulkItem replaceId(CcpBulkItem item) {
		
		boolean isAnotherEntity = this.isAnotherEntity(item);
		
		if(isAnotherEntity) {
			return item;
		}
		
		String id = this.calculateId(item.json);
		CcpEntityMetaData entityMetaData = this.getEntityMetaData();
		CcpBulkItem itemWithReplacedId = new CcpBulkItem(item.json, item.operation, entityMetaData.entity, id);
		return itemWithReplacedId;
		
	}

	/**
	 * Tells whether the item belongs to neither this entity nor its twin.
	 * @param item the bulk item
	 * @return {@code true} for another entity
	 */
	private boolean isAnotherEntity(CcpBulkItem item) {
		
		CcpEntityMetaData thisEntityDetails = this.getEntityMetaData();
		CcpEntityMetaData itemEntityDetails = item.entity.getEntityMetaData();
		
		boolean isThisEntity = itemEntityDetails.entityName.equals(thisEntityDetails.entityName);
		if(isThisEntity) {
			return false;
		}
		CcpEntityMetaData thisEntityMetaData = this.getEntityMetaData();
		CcpEntity twinEntity = thisEntityMetaData.entity.getTwinEntity();

		CcpEntityMetaData twinEntityDetails = twinEntity.getEntityMetaData();
		boolean isTwinEntity = itemEntityDetails.entityName.equals(twinEntityDetails.entityName);
		if(isTwinEntity) {
			return false;
		}
		return true;
	}

	/**
	 * The items of the wrapped entity, with the id of the current period, plus the item of the copy.
	 * @param json the record
	 * @param operation the bulk operation
	 * @return the bulk items
	 */
	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> originalBulkItems = this.entity.toBulkItems(json, operation);
		Stream<CcpBulkItem> originalBulkItemsStream = originalBulkItems
				.stream();
				var bulkItemsWithReplacedIdStream = originalBulkItemsStream
				.map(item -> this.replaceId(item));
				List<CcpBulkItem> bulkItems = bulkItemsWithReplacedIdStream
				.collect(Collectors.toList())
				;
		ArrayList<CcpBulkItem> items = new ArrayList<>(bulkItems);
		CcpBulkItem expurgableToBulkOperation = this.getExpurgableToBulkOperation(json, operation);
		items.add(expurgableToBulkOperation);
		return items;
	}

	/**
	 * Returns the key of the copy of the record ({@code id} and {@code entity}), after the field transformers.
	 * @param json the record
	 * @return the key of the copy
	 */
	public CcpJsonRepresentation getIdToSearchDisposableRecord(CcpJsonRepresentation json) {
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
		
		Supplier<CcpJsonRepresentation> jsonSupplier = handledJson.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(jsonSupplier);

		String id = primaryKeyValues.asUgglyJson();
		CcpJsonRepresentation jsonWithId = CcpOtherConstants
				.EMPTY_JSON
				.put(JnJsonCommonsFields.id, id);

				CcpJsonRepresentation idToSearch = jsonWithId
				.put(JnJsonCommonsFields.entity, entityDetails.entityName)
				;
		return idToSearch;
	}
	
	/**
	 * Returns the document of the current period from the search result.
	 * @param unionAll the search result
	 * @param jsonSupplier supplies the record
	 * @return the document, or an empty JSON
	 */
	public CcpJsonRepresentation getRecordFromUnionAll(CcpSelectUnionAll unionAll, Supplier<CcpJsonRepresentation> jsonSupplier) {
 
		CcpJsonRepresentation json = jsonSupplier.get();

		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
		
		String id = this.calculateId(handledJson);
		
		CcpJsonRepresentation jsonValue = unionAll.getEntityRow(entityDetails.entityName, id);
		
		return jsonValue;
	}

	
}
