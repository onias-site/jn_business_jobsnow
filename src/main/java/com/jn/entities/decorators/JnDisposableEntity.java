package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpDynamicJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDetails;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.utils.CcpHashAlgorithm;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnDisposableEntity extends CcpDefaultEntityDelegator<CcpEntityDisposable>{
	
	private final CcpEntityExpurgableOptions timeOption;
	final Class<?>  clazz;

	protected JnDisposableEntity(CcpEntity entity, Class<?> clazz) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
		this.timeOption = clazz.getAnnotation(CcpEntityDisposable.class).expurgTime();
		this.clazz = clazz;
	}
	
	private CcpJsonRepresentation getExpurgableId(CcpJsonRepresentation json) {
		
		CcpEntityDetails entityDetails = this.getEntityDetails();
		String id = entityDetails.getPrimaryKeyValues(json).asUgglyJson();
		
		
		CcpJsonRepresentation expurgableId = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityDisposableRecord.Fields.entity, entityDetails.entityName)
				.put(JnEntityDisposableRecord.Fields.id, id)
				;
		return expurgableId;
	}
	
	private String extractFormatedCurrentTimestamp(CcpJsonRepresentation json) {
		try {
			Long timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, () -> System.currentTimeMillis());
			String formattedTimestamp = this.timeOption.getFormattedDate(timestamp);
			return formattedTimestamp;
			
		} catch (Exception e) {
			String timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, () -> "" + System.currentTimeMillis());
			Long valueOf = Long.valueOf(timestamp);
			String formattedTimestamp = this.timeOption.getFormattedDate(valueOf);
			return formattedTimestamp;
		}
	}
	private boolean isValidTimestamp(CcpJsonRepresentation requiredEntityRow) {
		
		String timeStampFieldName = JnEntityDisposableRecord.Fields.timestamp.name();
		
		boolean recordNotFound = false == requiredEntityRow.getDynamicVersion().containsAllFields(timeStampFieldName);
	
		if(recordNotFound) {
			return false;
		}
		
		Long timeStamp = requiredEntityRow.getDynamicVersion().getAsLongNumber(timeStampFieldName);
		
		if(timeStamp > System.currentTimeMillis()) {
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
		CcpEntityDetails entityDetails = this.getEntityDetails();
		String id = entityDetails.getPrimaryKeyValues(json).asUgglyJson();
		Long timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, () -> System.currentTimeMillis());
		CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(json);
		Long nextTimeStamp = this.timeOption.getNextTimeStamp(timestamp);
		String nextDate = this.timeOption.getNextDate(timestamp);
		CcpJsonRepresentation expurgable = expurgableId
				.put(JnEntityDisposableRecord.Fields.format, this.timeOption.format)
				.put(JnEntityDisposableRecord.Fields.timestamp, nextTimeStamp)
				.put(JnEntityDisposableRecord.Fields.json,onlyExistingFields)
				.put(JnEntityDisposableRecord.Fields.date, nextDate)
				.put(JnEntityDisposableRecord.Fields.id, id)
				;
		return expurgable;
	}

	public String calculateId(CcpJsonRepresentation json) {

		String formattedTimestamp = this.extractFormatedCurrentTimestamp(json);
		String calculateId = this.entity.calculateId(json);

		ArrayList<Object> onlyPrimaryKeysValues = new ArrayList<>();
		onlyPrimaryKeysValues.add(formattedTimestamp);
		onlyPrimaryKeysValues.add(calculateId);
		
		String replace = onlyPrimaryKeysValues.toString().replace("[", "").replace("]", "");
		CcpHashDecorator hash2 = new CcpStringDecorator(replace).hash();
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
	
		boolean isNotPresentInCopyEntity = false == JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);
		
		if(isNotPresentInCopyEntity) {
			return false;
		}
		
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, expurgableId);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnEntityDisposableRecord.Fields.timestamp);
		
		boolean obsoleteTimeStamp = timeStamp <= System.currentTimeMillis();
		
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
	
		boolean isNotPresentInCopyEntity = false == JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, allValuesTogether);

		if(isNotPresentInCopyEntity) {
			CcpJsonRepresentation oneById =  this.entity.getOneById(json);
			return oneById;
		}

		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, allValuesTogether);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnEntityDisposableRecord.Fields.timestamp);
		
		boolean validTimeStamp = timeStamp > System.currentTimeMillis();
		
		if(validTimeStamp) {
			CcpJsonRepresentation innerJson = requiredEntityRow.getInnerJson(JnEntityDisposableRecord.Fields.json);
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
		
		CcpDynamicJsonRepresentation dynamicVersion = parameterToSearch.getDynamicVersion();
		String entityName = dynamicVersion.getAsString(fieldNameToEntity);
		
		CcpEntityDetails entityDetails = this.getEntityDetails();
		boolean isAnotherEntity = false == entityName.equals(entityDetails.entityName);
	
		if(isAnotherEntity) {
			return parameterToSearch;
		}
		
		String fieldNameToId = dependency.getFieldNameToId();
		String id = this.calculateId(json);
		CcpJsonRepresentation put = dynamicVersion.put(fieldNameToId, id);
		return put;
	}
	
	public List<CcpJsonRepresentation> getParametersToSearch(CcpJsonRepresentation json) {
		
		List<CcpJsonRepresentation> mainParametersToSearch =  this.entity.getParametersToSearch(json)
				.stream()
				.map(p -> this.replaceParameterToSearch(p, json))
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
		CcpEntityDetails entityDetails = this.getEntityDetails();
		
		CcpJsonRepresentation recordFromUnionAll = unionAll.getEntityRow(entityDetails.entityName, id);

		boolean recordFound = false == recordFromUnionAll.isEmpty();
		
		if(recordFound) {
			return recordFromUnionAll;
		}

		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpJsonRepresentation recordFromDisposable = JnEntityDisposableRecord.ENTITY.getRecordFromUnionAll(unionAll, expurgableId);
		
		boolean isInvalid = false == this.isValidTimestamp(recordFromDisposable);
	
		if(isInvalid) {
			throw new CcpErrorBulkEntityRecordNotFound(JnEntityDisposableRecord.ENTITY, expurgableId);
		}
		
		CcpJsonRepresentation innerJson = recordFromDisposable.getInnerJson(JnEntityDisposableRecord.Fields.json);
		return innerJson;
	}
	
	public boolean isPresentInThisUnionAll(CcpSelectUnionAll unionAll, CcpJsonRepresentation json) {

		String id = this.calculateId(json);

		CcpEntityDetails entityDetails = this.getEntityDetails();
		
		boolean presentInThisUnionAll = unionAll.isPresent(entityDetails.entityName, id);

		if(presentInThisUnionAll) {
			return true;
		}
		
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);

		boolean notFoundInDisposable = false == JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, expurgableId);
		
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
		CcpBulkItem ccpBulkItem = new CcpBulkItem(item.json, item.operation, this.getEntityDetails().entity, id);
		return ccpBulkItem;
		
	}

	private boolean isAnotherEntity(CcpBulkItem item) {
		
		CcpEntityDetails thisEntityDetails = this.getEntityDetails();
		CcpEntityDetails itemEntityDetails = item.entity.getEntityDetails();
		
		boolean isThisEntity = itemEntityDetails.entityName.equals(thisEntityDetails.entityName);
		if(isThisEntity) {
			return false;
		}

		CcpEntityDetails twinEntityDetails = this.getEntityDetails().entity.getTwinEntity().getEntityDetails();
		boolean isTwinEntity = itemEntityDetails.entityName.equals(twinEntityDetails.entityName);
		if(isTwinEntity) {
			return false;
		}
		
		return true;
	}
	
	

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems2 = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> bulkItems = bulkItems2
				.stream()
				.map(item -> this.replaceId(item))
				.collect(Collectors.toList())
				;
		ArrayList<CcpBulkItem> items = new ArrayList<>(bulkItems);
		CcpBulkItem expurgableToBulkOperation = this.getExpurgableToBulkOperation(json, operation);
		items.add(expurgableToBulkOperation);
		return items;
	}

}
