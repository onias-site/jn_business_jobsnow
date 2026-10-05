package com.jn.db.bulk;

import java.util.function.Function;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.json.fields.validation.JnJsonCommonsFields;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * Mapping function used by JnExecuteBulkOperation to convert a failed bulk operation
 * result into a reprocessing record (JnEntityRecordToReprocess).
 * Prevents infinite loops by rejecting items that already belong to the reprocessing entity.
 */
class FunctionReprocessMapper implements Function<CcpBulkOperationResult, CcpJsonRepresentation>{

	/** The single instance. */
	public static final FunctionReprocessMapper INSTANCE = new FunctionReprocessMapper();
	
	/** Singleton; use {@link #INSTANCE}. */
	private FunctionReprocessMapper() {}

	/**
	 * Converts a failed bulk item into a {@code jn_record_to_reprocess} record: the item fields, the current
	 * {@code timestamp}, the error details (with {@code type} renamed to {@code errorType}), the {@code id} of the item and
	 * the name of its entity, reduced to the fields of the entity.
	 * @param result the failed result
	 * @return the record to reprocess
	 * @throws JnErrorReprocessInfiniteLoopPrevented when the failed item is itself a record to reprocess
	 */
	public CcpJsonRepresentation apply(CcpBulkOperationResult result) {
		CcpBulkItem bulkItem = result.getBulkItem();
		CcpEntityMetaData entityDetails = bulkItem.entity.getEntityMetaData();
		CcpEntityMetaData entityMetaData = JnEntityRecordToReprocess.ENTITY.getEntityMetaData();
		boolean itIsTryingToStartAnInfinitLoop = entityDetails.entityName.equals(entityMetaData.entityName);
		if(itIsTryingToStartAnInfinitLoop) {
			JnErrorReprocessInfiniteLoopPrevented jnErrorReprocessInfiniteLoopPrevented = new JnErrorReprocessInfiniteLoopPrevented();
			throw jnErrorReprocessInfiniteLoopPrevented;
		}
		long currentTimeMillis = System.currentTimeMillis();
		CcpJsonRepresentation jsonWithTimestamp = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, currentTimeMillis);
		CcpJsonRepresentation jsonWithItem = jsonWithTimestamp.mergeWithAnotherJson(bulkItem.json);
		CcpJsonRepresentation errorDetails = result.getErrorDetails();
		CcpJsonRepresentation jsonWithErrorDetails = jsonWithItem.mergeWithAnotherJson(errorDetails);
		CcpJsonRepresentation jsonWithErrorType = jsonWithErrorDetails.renameField(CcpJsonCommonsFields.type, JnEntityRecordToReprocess.Fields.errorType);
		CcpJsonRepresentation jsonWithId = jsonWithErrorType.put(JnJsonCommonsFields.id, bulkItem.id);
		CcpJsonRepresentation jsonWithEntity = jsonWithId.put(JnJsonCommonsFields.entity, entityDetails.entityName);
		var reprocessFields = JnEntityRecordToReprocess.Fields.values();
		CcpJsonRepresentation recordToReprocess = jsonWithEntity
		.getJsonPiece(reprocessFields);
		return recordToReprocess;
	}

	/** Raised when a record to reprocess fails, so it is not turned into another record to reprocess forever. */
	@SuppressWarnings("serial")
	private static class JnErrorReprocessInfiniteLoopPrevented extends RuntimeException {
	}
}
