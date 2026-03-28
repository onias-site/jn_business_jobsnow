package com.jn.entities.decorators2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity2;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityExpurgable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpDecoratorEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpEntityDetails;
import com.ccp.utils.CcpHashAlgorithm;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnExpurgableEntity extends CcpEntityDelegator implements CcpDecoratorEntity<CcpEntityExpurgable>{
	
	final Class<?>  clazz;
	private final CcpEntityExpurgableOptions timeOption;

	private JnExpurgableEntity() {
		super(null);
		this.timeOption = null;
		this.clazz = null;
	}
	
	protected JnExpurgableEntity(CcpEntity2 entity, Class<?> clazz, CcpEntityExpurgableOptions timeOption) {
		super(entity);
		this.timeOption = timeOption;
		this.clazz = clazz;

	}
	
	public boolean isThisEntityDecorated(Class<CcpEntityExpurgable> annotation) {
		boolean annotationPresent = this.clazz.isAnnotationPresent(annotation);
		return annotationPresent;
	}

	public List<CcpBusiness> getFlow() {
		return new ArrayList<>();
	}

	public Map<Class<?>, List<CcpBusiness>> getExceptionHandlers() {
		Map<Class<?>, List<CcpBusiness>> result = new HashMap<>();
		return result;
	}

	public CcpEntityExpurgable getAnnotation() {
		CcpEntityExpurgable annotation = this.clazz.getAnnotation(CcpEntityExpurgable.class);
		return annotation;
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
			Long timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, System.currentTimeMillis());
			String formattedTimestamp = this.timeOption.getFormattedDate(timestamp);
			return formattedTimestamp;
			
		} catch (Exception e) {
			String timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, "" + System.currentTimeMillis());
			Long valueOf = Long.valueOf(timestamp);
			String formattedTimestamp = this.timeOption.getFormattedDate(valueOf);
			return formattedTimestamp;
		}
	}
	private List<CcpJsonRepresentation> getParametersToSearchExpurgable(CcpJsonRepresentation json) {
		
		String id = this.calculateId(json);

		CcpDbRequester dependency = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		
		String fieldNameToEntity = dependency.getFieldNameToEntity();
		String fieldNameToId = dependency.getFieldNameToId();
		
		CcpEntityDetails entityDetails = this.getEntityDetails();
		
		CcpJsonRepresentation mainRecord = CcpOtherConstants.EMPTY_JSON
				.getDynamicVersion().put(fieldNameToEntity, entityDetails.entityName)
				.getDynamicVersion().put(fieldNameToId, id)
		;
		List<CcpJsonRepresentation> asList = Arrays.asList(mainRecord);
		return asList;
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
		Long timestamp = json.getOrDefault(JnEntityDisposableRecord.Fields.timestamp, System.currentTimeMillis());
		Long nextTimeStamp = this.timeOption.getNextTimeStamp(timestamp);
		String nextDate = this.timeOption.getNextDate(timestamp);
		CcpJsonRepresentation expurgable = expurgableId
				.put(JnEntityDisposableRecord.Fields.format, this.timeOption.format)
				.put(JnEntityDisposableRecord.Fields.timestamp, nextTimeStamp)
				.put(JnEntityDisposableRecord.Fields.json, json)
				.put(JnEntityDisposableRecord.Fields.date, nextDate)
				.put(JnEntityDisposableRecord.Fields.id, id)
				;
		return expurgable;
	}


	public final String calculateId(CcpJsonRepresentation json) {

		String formattedTimestamp = this.extractFormatedCurrentTimestamp(json);

		ArrayList<Object> onlyPrimaryKeysValues = new ArrayList<>();
		onlyPrimaryKeysValues.add(formattedTimestamp);
		CcpEntityDetails entityDetails = this.getEntityDetails();
		ArrayList<Object> sortedPrimaryKeyValues = entityDetails.getSortedPrimaryKeyValues(json);
		onlyPrimaryKeysValues.addAll(sortedPrimaryKeyValues);
		
		String replace = onlyPrimaryKeysValues.toString().replace("[", "").replace("]", "");
		CcpHashDecorator hash2 = new CcpStringDecorator(replace).hash();
		String hash = hash2.asString(CcpHashAlgorithm.SHA1);
		return hash;
	}

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.toDeleteBulkItems(json);
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems);
		return json;
	}
	
	public String[] getEntitiesToSelect() {
		// FIXME
		return super.getEntitiesToSelect();
	}

	public CcpJsonRepresentation getOneById(CcpJsonRepresentation json) {
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation expurgableId = this.getExpurgableId(json);
		CcpJsonRepresentation allValuesTogether = expurgableId.mergeWithAnotherJson(json);
		//FIXME
		CcpSelectUnionAll unionAll = crud.unionAll(allValuesTogether, JnDeleteKeysFromCache.INSTANCE, null, JnEntityDisposableRecord.ENTITY);

		boolean isPresentInOriginalEntity = this.isPresentInThisUnionAll(unionAll, allValuesTogether);
		
		if(isPresentInOriginalEntity) {
			CcpJsonRepresentation requiredEntityRow = this.getRequiredEntityRow(unionAll, allValuesTogether);
			return requiredEntityRow;
		}
	
		boolean isNotPresentInCopyEntity = false == JnEntityDisposableRecord.ENTITY.isPresentInThisUnionAll(unionAll, allValuesTogether);

		if(isNotPresentInCopyEntity) {
		//FIXME
			//			String calculateId = this.calculateId(allValuesTogether);
//			CcpJsonRepresentation oneById =  this.entity.getOneById(calculateId);
//			return oneById;
		}

		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRequiredEntityRow(unionAll, allValuesTogether);
		Long timeStamp = requiredEntityRow.getAsLongNumber(JnEntityDisposableRecord.Fields.timestamp);
		
		boolean validTimeStamp = timeStamp > System.currentTimeMillis();
		
		if(validTimeStamp) {
			CcpJsonRepresentation innerJson = requiredEntityRow.getInnerJson(JnEntityDisposableRecord.Fields.json);
			return innerJson;
		}

		//FIXME
//		String calculateId = this.calculateId(allValuesTogether);
//		CcpJsonRepresentation oneById =  this.entity.getOneById(calculateId);
		
		return json;
	}
	
	public CcpJsonRepresentation getOneByIdAnywhere(CcpJsonRepresentation json) {
		// FIXME
		return super.getOneByIdAnywhere(json);
	}
	
	public List<CcpJsonRepresentation> getParametersToSearch(CcpJsonRepresentation json) {
		
		List<CcpJsonRepresentation> mainParametersToSearch =  this.getParametersToSearchExpurgable(json);
		
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
			//FIXME
			throw new CcpErrorBulkEntityRecordNotFound(null, expurgableId);
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
		
		CcpJsonRepresentation requiredEntityRow = JnEntityDisposableRecord.ENTITY.getRequiredEntityRow(unionAll, expurgableId);
		
		boolean valid = this.isValidTimestamp(requiredEntityRow);
		
		if(valid) {
			return true;
		}
		
		return false;
	}

	
	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.toSaveBulkItems(json);
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems);
		return json;
	}


	public List<CcpBulkItem> toDeleteBulkItems(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.toBulkItems(json, CcpBulkEntityOperationType.delete);
		return bulkItems;
	}

	public List<CcpBulkItem> toSaveBulkItems(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.toBulkItems(json, CcpBulkEntityOperationType.create);
		return bulkItems;
	}

	
	private List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {

		String mainEntityId = this.calculateId(json);
		//FIXME
		CcpBulkItem mainItem = new CcpBulkItem(json, operation, null, mainEntityId);
		CcpBulkItem expurgableToBulkOperation = this.getExpurgableToBulkOperation(json, operation);
		List<CcpBulkItem> asList = Arrays.asList(mainItem, expurgableToBulkOperation);
		return asList;
	}

}
