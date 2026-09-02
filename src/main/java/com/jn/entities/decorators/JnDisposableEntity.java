package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import java.util.stream.Stream;

/**
 * Decorador que implementa TTL (time-to-live) para entidades marcadas com {@code @CcpEntityDisposable}.
 * Em vez de depender de TTL nativo do Elasticsearch, armazena uma cópia do JSON em
 * {@code JnEntityDisposableRecord} com timestamp de expiração calculado conforme a opção de tempo
 * configurada ({@code hourly}, {@code daily}, etc.). Sobrescreve os métodos de leitura para
 * consultar o registro de expiração e validar se ainda está vigente.
 */
public class JnDisposableEntity extends CcpDefaultEntityDelegator<CcpEntityDisposable>{
	
	private final CcpEntityExpurgableOptions timeOption;
	final Class<?>  clazz;

	protected JnDisposableEntity(CcpEntity entity, Class<?> clazz) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
		this.timeOption = clazz.getAnnotation(CcpEntityDisposable.class).expurgTime();
		this.clazz = clazz;
	}
	
	private CcpJsonRepresentation getExpurgableId(CcpJsonRepresentation json) {
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();

		Supplier<CcpJsonRepresentation> supplier = json.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(supplier);
		String id = primaryKeyValues.asUgglyJson();
		CcpJsonRepresentation put2 = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);

				CcpJsonRepresentation expurgableId = put2
				.put(JnJsonCommonsFields.id, id)
				;
		return expurgableId;
	}
	
	private String extractFormatedCurrentTimestamp(CcpJsonRepresentation json) {
		long currentTimeMillis = System.currentTimeMillis();
		String formattedTimestamp = this.timeOption.getFormattedDate(currentTimeMillis);
		return formattedTimestamp;
	}
	
	
	private boolean isValidTimestamp(CcpJsonRepresentation requiredEntityRow) {
		
		String timeStampFieldName = JnJsonCommonsFields.timestamp.name();
		CcpFieldName ccpFieldName = new CcpFieldName(timeStampFieldName);
		boolean containsAllFields = requiredEntityRow.containsAllFields(ccpFieldName);

		boolean recordNotFound = false == containsAllFields;

		if(recordNotFound) {
			return false;
		}
		CcpFieldName ccpFieldName2 = new CcpFieldName(timeStampFieldName);

		Long timeStamp = requiredEntityRow.getAsLongNumber(ccpFieldName2);
		long currentTimeMillis2 = System.currentTimeMillis();
		boolean timeStampMaior = timeStamp > currentTimeMillis2;

		if(timeStampMaior) {
			return true;
		}
		return false;
	}
	private final CcpBulkItem getExpurgableToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation recordCopy = this.populateAnExpurgableFromJson(json);
		String calculateId = JnEntityDisposableRecord.ENTITY.calculateId(recordCopy);
		CcpBulkItem ccpBulkItem = new CcpBulkItem(recordCopy, operation, JnEntityDisposableRecord.ENTITY, calculateId);
		
		return ccpBulkItem;
	}
	
	private CcpJsonRepresentation populateAnExpurgableFromJson(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues2 = entityDetails.getPrimaryKeyValues(jsonSupplier);
		String id = primaryKeyValues2.asUgglyJson();
		Long timestamp = json.getOrDefault(JnJsonCommonsFields.timestamp, () -> System.currentTimeMillis());
		CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(json);
		Long nextTimeStamp = this.timeOption.getNextTimeStamp(timestamp);
		String nextDate = this.timeOption.getNextDate(timestamp);
		CcpJsonRepresentation put3 = expurgableId
				.put(JnEntityDisposableRecord.Fields.format, this.timeOption.format);
				CcpJsonRepresentation put4 = put3
				.put(JnJsonCommonsFields.timestamp, nextTimeStamp);
				CcpJsonRepresentation put5 = put4
				.put(JnEntityDisposableRecord.Fields.trueTimestamp, timestamp);
				CcpJsonRepresentation put6 = put5
				.put(JnJsonCommonsFields.json,onlyExistingFields);
				CcpJsonRepresentation put7 = put6
				.put(JnJsonCommonsFields.date, nextDate);

				CcpJsonRepresentation expurgable = put7
				.put(JnJsonCommonsFields.id, id)
				;
		return expurgable;
	}

	public String calculateId(CcpJsonRepresentation json) {

		String formattedTimestamp = this.extractFormatedCurrentTimestamp(json);
		String calculateId = this.entity.calculateId(json);

		ArrayList<Object> onlyPrimaryKeysValues = new ArrayList<>();
		onlyPrimaryKeysValues.add(formattedTimestamp);
		onlyPrimaryKeysValues.add(calculateId);
		String toString = onlyPrimaryKeysValues.toString();
		String toStringReplace = toString.replace("[", "");

		String replace = toStringReplace.replace("]", "");
		CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(replace);
		CcpHashDecorator hash2 = ccpStringDecorator.hash();
		String hash = hash2.asString(CcpHashAlgorithm.SHA1);
		return hash;
	}

	public boolean exists(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		CcpSelectUnionAll unionAll = crud.unionAll(allValuesTogether, JnDeleteKeysFromCache.INSTANCE, this, JnEntityDisposableRecord.ENTITY);

		boolean isPresentInOriginalEntity = this.isPresentInThisUnionAll(unionAll, allValuesTogether);
		
		if(isPresentInOriginalEntity) {
			return true;
		}
		boolean presentInThisUnionAll2 = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);

		boolean isNotPresentInCopyEntity = false == presentInThisUnionAll2;
		
		if(isNotPresentInCopyEntity) {
			return false;
		}
		
		Supplier<CcpJsonRepresentation> jsonSupplier = expurgableId.getJsonSupplier();
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, jsonSupplier);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnJsonCommonsFields.timestamp);
		long currentTimeMillis3 = System.currentTimeMillis();

		boolean obsoleteTimeStamp = timeStamp <= currentTimeMillis3;
		
		if(obsoleteTimeStamp) {
			return false;
		}
		return true;
	}
	
	public List<CcpEntity> getAssociatedEntities() {
		List<CcpEntity> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity> result = new ArrayList<CcpEntity>(associatedEntities);
		result.add(JnEntityDisposableRecord.ENTITY);
		return result;
	}
	
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
		boolean presentInThisUnionAll3 = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, allValuesTogether);

		boolean isNotPresentInCopyEntity = false == presentInThisUnionAll3;

		if(isNotPresentInCopyEntity) {
			CcpJsonRepresentation oneById =  this.entity.getOneById(json);
			return oneById;
		}

		Supplier<CcpJsonRepresentation> jsonSupplier = allValuesTogether.getJsonSupplier();
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, jsonSupplier);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnJsonCommonsFields.timestamp);
		long currentTimeMillis4 = System.currentTimeMillis();

		boolean validTimeStamp = timeStamp > currentTimeMillis4;
		
		if(validTimeStamp) {
			CcpJsonRepresentation innerJson = requiredEntityRow.getInnerJson(JnJsonCommonsFields.json);
			return innerJson;
		}

		CcpJsonRepresentation oneById =  this.entity.getOneById(json);
		return oneById;
	}
	
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		
		CcpJsonRepresentation result = super.getOneByIdAnyWhere(allValuesTogether);
		return result;
	}
	
	private CcpJsonRepresentation replaceParameterToSearch(CcpJsonRepresentation parameterToSearch, CcpJsonRepresentation json) {

		CcpDbRequester dependency = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		String fieldNameToEntity = dependency.getFieldNameToEntity();
		CcpFieldName ccpFieldName3 = new CcpFieldName(fieldNameToEntity);

		String entityName = parameterToSearch.getAsString(ccpFieldName3);
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		boolean entityNameEquals = entityName.equals(entityDetails.entityName);
		boolean isAnotherEntity = false == entityNameEquals;
	
		if(isAnotherEntity) {
			return parameterToSearch;
		}
		
		String fieldNameToId = dependency.getFieldNameToId();
		String id = this.calculateId(json);
		CcpFieldName ccpFieldName4 = new CcpFieldName(fieldNameToId);
		CcpJsonRepresentation put = parameterToSearch.put(ccpFieldName4, id);
		return put;
	}
	
	public List<CcpJsonRepresentation> getParametersToSearch(CcpJsonRepresentation json) {
		List<CcpJsonRepresentation> parametersToSearch = this.entity.getParametersToSearch(json);
		Stream<CcpJsonRepresentation> stream = parametersToSearch
				.stream();
				var streamMap = stream
				.map(p -> this.replaceParameterToSearch(p, json));

				List<CcpJsonRepresentation> mainParametersToSearch =  streamMap
				.collect(Collectors.toList())
				;
		
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		List<CcpJsonRepresentation> othersParametersToSearch = JnEntityDisposableRecord.ENTITY.getParametersToSearch(expurgableId);
		ArrayList<CcpJsonRepresentation> result = new ArrayList<>();
		result.addAll(othersParametersToSearch);
		result.addAll(mainParametersToSearch);
		return result;
	}

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
	
	public boolean isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json) {

		String id = this.calculateId(json);

		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		boolean presentInThisUnionAll = unionAll.isPresent(entityDetails.entityName, id);

		if(presentInThisUnionAll) {
			return true;
		}
		
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		boolean presentInThisUnionAll4 = JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);

		boolean notFoundInDisposable = false == presentInThisUnionAll4;
		
		if(notFoundInDisposable) {
			return false;
		}
		
		CcpJsonRepresentation mergeWithAnotherJson = expurgableId.mergeWithAnotherJson(json);
		
		CcpJsonRepresentation requiredEntityRow = this.getRecordFromUnionAll(unionAll, mergeWithAnotherJson);
		
		boolean valid = this.isValidTimestamp(requiredEntityRow);
		
		if(valid) {
			return true;
		}
		return false;
	}

	private CcpBulkItem replaceId(CcpBulkItem item) {
		
		boolean isAnotherEntity = this.isAnotherEntity(item);
		
		if(isAnotherEntity) {
			return item;
		}
		
		String id = this.calculateId(item.json);
		CcpEntityMetaData entityMetaData = this.getEntityMetaData();
		CcpBulkItem ccpBulkItem = new CcpBulkItem(item.json, item.operation, entityMetaData.entity, id);
		return ccpBulkItem;
		
	}

	private boolean isAnotherEntity(CcpBulkItem item) {
		
		CcpEntityMetaData thisEntityDetails = this.getEntityMetaData();
		CcpEntityMetaData itemEntityDetails = item.entity.getEntityMetaData();
		
		boolean isThisEntity = itemEntityDetails.entityName.equals(thisEntityDetails.entityName);
		if(isThisEntity) {
			return false;
		}
		CcpEntityMetaData entityMetaData2 = this.getEntityMetaData();
		CcpEntity twinEntity = entityMetaData2.entity.getTwinEntity();

		CcpEntityMetaData twinEntityDetails = twinEntity.getEntityMetaData();
		boolean isTwinEntity = itemEntityDetails.entityName.equals(twinEntityDetails.entityName);
		if(isTwinEntity) {
			return false;
		}
		return true;
	}

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems2 = this.entity.toBulkItems(json, operation);
		Stream<CcpBulkItem> stream2 = bulkItems2
				.stream();
				var stream2Map = stream2
				.map(item -> this.replaceId(item));
				List<CcpBulkItem> bulkItems = stream2Map
				.collect(Collectors.toList())
				;
		ArrayList<CcpBulkItem> items = new ArrayList<>(bulkItems);
		CcpBulkItem expurgableToBulkOperation = this.getExpurgableToBulkOperation(json, operation);
		items.add(expurgableToBulkOperation);
		return items;
	}

	public CcpJsonRepresentation getIdToSearchDisposableRecord(CcpJsonRepresentation json) {
		
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
		
		Supplier<CcpJsonRepresentation> jsonSupplier = handledJson.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues3 = entityDetails.getPrimaryKeyValues(jsonSupplier);

		String id = primaryKeyValues3.asUgglyJson();
		CcpJsonRepresentation put8 = CcpOtherConstants
				.EMPTY_JSON
				.put(JnJsonCommonsFields.id, id);

				CcpJsonRepresentation idToSearch = put8
				.put(JnJsonCommonsFields.entity, entityDetails.entityName)
				;
		return idToSearch;
	}
	
	public CcpJsonRepresentation getRecordFromUnionAll(CcpSelectUnionAll unionAll, Supplier<CcpJsonRepresentation> jsonSupplier) {

		CcpJsonRepresentation json = jsonSupplier.get();

		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		
		CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
		
		String id = this.calculateId(handledJson);
		
		CcpJsonRepresentation jsonValue = unionAll.getEntityRow(entityDetails.entityName, id);
		
		return jsonValue;
	}

	
}
