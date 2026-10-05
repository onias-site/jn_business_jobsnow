package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityVersionable;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Versioning decorator ({@code @JnEntityVersionable}): every bulk write of the record also creates a history row in
 * {@code jn_versionable} with the state of the record before the write (the stored record, or the given fields when it
 * does not exist yet), the operation, the date and time, the entity name and the serialized primary key.
 */
public class JnVersionableEntity extends CcpDefaultEntityDelegator<Object>{
	
	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 */
	public JnVersionableEntity(CcpEntity entity) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
	}

	/**
	 * Builds the {@code create} bulk item of the history row.
	 * @param json the record
	 * @param operation the bulk operation being recorded
	 * @return the bulk item of the history row
	 */
	private final CcpBulkItem getVersionableToBulkOperationToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation versionable = this.getVersionableRecord(json, operation);
		String historyRecordId = JnEntityVersionable.ENTITY.calculateId(versionable);
		CcpBulkItem historyBulkItem = new CcpBulkItem(versionable, CcpBulkEntityOperationType.create, JnEntityVersionable.ENTITY, historyRecordId);
				
		return historyBulkItem;
	}

	/**
	 * Returns the record that will be pictured in the history row: the one saved in the database when
	 * it exists, or the entity fields present in the json when it does not exist yet.
	 */
	private static CcpJsonRepresentation getRecordToAudit(CcpEntityMetaData entityDetails, CcpJsonRepresentation json) {

		CcpBusiness ifNotFound = x ->

		{
			CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
			CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(handledJson);
			return onlyExistingFields;
		};

		CcpJsonRepresentation recordToAudit = entityDetails.getOneByIdOrHandleItIfThisIdWasNotFound(json, ifNotFound);

		return recordToAudit;
	}

	/**
	 * Computes the value saved in the {@code id} field of the history row: the record's serialized
	 * primary key. It is by this value, together with the entity name, that a record's history is found.
	 */
	private static String getVersionableRecordId(CcpEntityMetaData entityDetails, CcpJsonRepresentation recordToAudit) {

		Supplier<CcpJsonRepresentation> jsonSupplier = recordToAudit.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(jsonSupplier);

		String id = primaryKeyValues.asUgglyJson();

		return id;
	}

	/**
	 * Builds the history row: {@code id}, {@code json} (the record as text), {@code operation}, {@code date},
	 * {@code entity} and {@code timestamp}.
	 * @param json the record
	 * @param operation the bulk operation being recorded
	 * @return the history row
	 */
	private CcpJsonRepresentation getVersionableRecord(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {

		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();

		CcpJsonRepresentation recordToAudit = getRecordToAudit(entityDetails, json);

		String id = getVersionableRecordId(entityDetails, recordToAudit);
		CcpTimeDecorator currentTime = new CcpTimeDecorator();

		String formattedDateTime = currentTime.getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS");
		CcpJsonRepresentation jsonWithId = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.id, id);
				String recordAsText = "" + recordToAudit;
				CcpJsonRepresentation jsonWithRecord = jsonWithId
				.put(JnJsonCommonsFields.json, recordAsText);
				CcpJsonRepresentation jsonWithOperation = jsonWithRecord
				.put(JnJsonCommonsFields.operation, operation);
				CcpJsonRepresentation jsonWithDate = jsonWithOperation
				.put(JnJsonCommonsFields.date, formattedDateTime);
				CcpJsonRepresentation jsonWithEntity = jsonWithDate
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);
				long currentTimeMillis = System.currentTimeMillis();

		CcpJsonRepresentation audit = 
				jsonWithEntity
				.put(JnJsonCommonsFields.timestamp, currentTimeMillis)
		;
		return audit;
	}



	/**
	 * Builds the message consumed by {@code JnBusinessDeleteVersionableRecords}: the {@code id} each
	 * history row stores and the names under which they may have been saved (main and twin).
	 *
	 * <p>The one that enqueues the purge is {@code JnVersionablePurgeEntity}, not this decorator: this one
	 * sits inside the twin, and the twin's {@code deleteAnyWhere} never reaches it. The building lives here
	 * because it uses the same {@code id} computation as the saving of the history rows — both sides
	 * must agree, otherwise the purge does not find what was saved. It must be called before the
	 * deletion: the saved record is read to extract the primary key.
	 */
	static CcpJsonRepresentation getDeletionRequest(CcpEntity entity, CcpJsonRepresentation json) {

		CcpEntityMetaData entityDetails = entity.getEntityMetaData();

		CcpJsonRepresentation recordToDelete = getRecordToAudit(entityDetails, json);

		String versionableRecordId = getVersionableRecordId(entityDetails, recordToDelete);

		String[] entitiesToDelete = getEntitiesToDelete(entityDetails);

		CcpJsonRepresentation withEntityName = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entityDetails.entityName);
				CcpJsonRepresentation withRecordId = withEntityName
				.put(JnJsonCommonsFields.id, versionableRecordId);

		CcpJsonRepresentation deletionRequest = withRecordId
				.put(JnBusinessDeleteVersionableRecords.JsonFieldNames.entitiesToDelete, entitiesToDelete);

		return deletionRequest;
	}

	/**
	 * Lists the tables from which the record itself must be deleted: the main one and the twin, when it exists.
	 * {@code JnEntityVersionable} is removed from the list because its rows are not found by the document
	 * id, but by the pair ({@code entity}, {@code id}) — they are deleted by the other criterion of the purge
	 * query, which {@code JnBusinessDeleteVersionableRecords} itself adds.
	 */
	private static String[] getEntitiesToDelete(CcpEntityMetaData entityDetails) {

		CcpEntityMetaData versionableMetaData = JnEntityVersionable.ENTITY.getEntityMetaData();

		String[] entitiesToSelect = entityDetails.getEntitiesToSelect();
		List<String> allEntities = Arrays.asList(entitiesToSelect);
		Stream<String> allEntitiesStream = allEntities.stream();
		var withoutTheHistory = allEntitiesStream.filter(x -> false == x.equals(versionableMetaData.entityName));

		List<String> entitiesToDelete = withoutTheHistory.collect(Collectors.toList());
		int size = entitiesToDelete.size();

		String[] entitiesToDeleteArray = entitiesToDelete.toArray(new String[size]);

		return entitiesToDeleteArray;
	}
	
	/**
	 * The associated entities of the wrapped entity plus {@code jn_versionable}.
	 * @return the associated entities
	 */
	public List<CcpEntity> getAssociatedEntities() {
		List<CcpEntity> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity> result = new ArrayList<CcpEntity>(associatedEntities);
		result.add(JnEntityVersionable.ENTITY);
		return result;
	}
	
	/**
	 * Not supported on this decorator.
	 * @param json the record
	 * @return never returns
	 * @throws UnsupportedOperationException always
	 */
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		Object throwException = this.throwException();
		return (CcpJsonRepresentation)throwException;
	}
	

	/**
	 * The items of the wrapped entity plus the history row.
	 * @param json the record
	 * @param operation the bulk operation
	 * @return the bulk items
	 */
	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> bulkItemsWithHistory = new ArrayList<>(bulkItems);
		
		CcpBulkItem versionableToBulkOperation = this.getVersionableToBulkOperationToBulkOperation(json, operation);
		bulkItemsWithHistory.add(versionableToBulkOperation);
		return bulkItemsWithHistory;
	}

	
}
