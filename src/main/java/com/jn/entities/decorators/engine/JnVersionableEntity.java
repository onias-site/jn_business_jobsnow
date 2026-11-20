package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnVersionableHistory;

/**
 * Versioning decorator ({@code @CcpEntityVersionable}): every bulk write of the record also adds a version at the end of
 * the history of the record, a group of {@code jn_grouper} ({@link JnVersionableHistory}), in the same bulk: the state of
 * the record before the write (the stored record, or the given fields when it does not exist yet), the operation and the
 * date and time. Until 2026-11-20 each version was a row of its own in {@code jn_versionable}, found only by a query.
 * <p>
 * A bulk keeps a single item per document, so two writes of the same record in the same bulk leave only one version.
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
	 * Returns the record that will be pictured in the version: the one saved in the database when it exists, or the
	 * entity fields present in the json when it does not exist yet.
	 */
	private static CcpJsonRepresentation getRecordToAudit(CcpEntityMetaData entityDetails, CcpJsonRepresentation json) {

		CcpBusiness ifNotFound = new JnBusinessEntityFieldsOfTheJson(entityDetails, json);

		CcpJsonRepresentation recordToAudit = entityDetails.getOneByIdOrHandleItIfThisIdWasNotFound(json, ifNotFound);

		return recordToAudit;
	}

	/**
	 * The values of the primary key of the record.
	 * @param entityDetails the metadata of the entity
	 * @param recordToAudit the record
	 * @return the primary key values
	 */
	private static CcpJsonRepresentation getPrimaryKey(CcpEntityMetaData entityDetails, CcpJsonRepresentation recordToAudit) {
		Supplier<CcpJsonRepresentation> jsonSupplier = recordToAudit.getJsonSupplier();
		CcpJsonRepresentation primaryKeyValues = entityDetails.getPrimaryKeyValues(jsonSupplier);
		return primaryKeyValues;
	}

	/**
	 * Builds the version: {@code operation}, {@code date}, {@code timestamp} and {@code json} (the record).
	 * @param recordToAudit the record before the write
	 * @param operation the bulk operation being recorded
	 * @return the version
	 */
	private static CcpJsonRepresentation getVersion(CcpJsonRepresentation recordToAudit, CcpBulkEntityOperationType operation) {
		CcpTimeDecorator currentTime = new CcpTimeDecorator();
		String formattedDateTime = currentTime.getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format);
		long currentTimeMillis = System.currentTimeMillis();
		String operationName = operation.name();
		CcpJsonRepresentation versionWithOperation = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.operation, operationName);
		CcpJsonRepresentation versionWithDate = versionWithOperation.put(JnJsonCommonsFields.date, formattedDateTime);
		CcpJsonRepresentation versionWithTimestamp = versionWithDate.put(JnJsonCommonsFields.timestamp, currentTimeMillis);
		CcpJsonRepresentation version = versionWithTimestamp.put(JnJsonCommonsFields.json, recordToAudit);
		return version;
	}

	/**
	 * Builds the bulk item that adds the version to the history of the record.
	 * @param json the record
	 * @param operation the bulk operation being recorded
	 * @return the bulk items of the history
	 */
	private List<CcpBulkItem> getItemsOfTheHistory(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();
		CcpJsonRepresentation recordToAudit = getRecordToAudit(entityDetails, json);
		CcpJsonRepresentation primaryKey = getPrimaryKey(entityDetails, recordToAudit);
		String recordId = entityDetails.entity.calculateId(recordToAudit);
		CcpJsonRepresentation version = getVersion(recordToAudit, operation);
		List<CcpBulkItem> itemsOfTheHistory = JnVersionableHistory.getItemsToAppendTheVersion(entityDetails.entityName, recordId, primaryKey, version);
		return itemsOfTheHistory;
	}

	/**
	 * Builds the message consumed by {@code JnBusinessDeleteVersionableRecords}: the document id of the record and the
	 * names under which its history may have been saved (main and twin).
	 *
	 * <p>The one that enqueues the purge is {@code JnVersionablePurgeEntity}, not this decorator: this one sits inside the
	 * twin, and the twin's {@code deleteAnyWhere} never reaches it. The building lives here because it uses the same id
	 * of the writes of the history: both sides must agree, otherwise the purge does not find what was saved. It must be
	 * called before the deletion: the saved record is read to calculate the id.
	 */
	static CcpJsonRepresentation getDeletionRequest(CcpEntity entity, CcpJsonRepresentation json) {

		CcpEntityMetaData entityDetails = entity.getEntityMetaData();

		CcpJsonRepresentation recordToDelete = getRecordToAudit(entityDetails, json);

		String recordId = entityDetails.entity.calculateId(recordToDelete);

		String[] entitiesToDelete = entityDetails.getEntitiesToSelect();

		CcpJsonRepresentation withRecordId = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.id, recordId);

		CcpJsonRepresentation deletionRequest = withRecordId
				.put(JnBusinessDeleteVersionableRecords.JsonFieldNames.entitiesToDelete, entitiesToDelete);

		return deletionRequest;
	}

	/**
	 * The items of the wrapped entity plus the version of the history.
	 * @param json the record
	 * @param operation the bulk operation
	 * @return the bulk items
	 */
	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> bulkItemsWithHistory = new ArrayList<>(bulkItems);
		List<CcpBulkItem> itemsOfTheHistory = this.getItemsOfTheHistory(json, operation);
		bulkItemsWithHistory.addAll(itemsOfTheHistory);
		return bulkItemsWithHistory;
	}
}
