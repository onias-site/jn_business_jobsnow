package com.jn.business.bots.stack.trace;

import java.util.Arrays;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Moves the error of the session ({@code errorId}) from {@code jn_jobsnow_error} to its twin,
 * {@code jn_jobsnow_error_solved}, when the operator of {@code /solveStackTrace} tells that it was solved.
 *
 * <p>The id of a disposable record depends on the period it was written in, and the ticket may be solved in a later
 * period, so the delete of the entity would calculate another id and miss the document. The move goes by the id the
 * command carries, in one bulk: the document is created in the twin with that id and deleted from the main entity. An
 * error no longer in the main entity (already solved) is left as it is.
 */
public class JnSupportStackTraceMoveToSolved implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JnSupportStackTraceMoveToSolved INSTANCE = new JnSupportStackTraceMoveToSolved();

	private JnSupportStackTraceMoveToSolved() {}

	/**
	 * Moves the error to the twin.
	 * @param json the session, with {@code errorId}
	 * @return the session as it came
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String errorId = json.getAsString(JnSupportStackTraceFields.errorId);
		String entityName = JnEntityJobsnowError.ENTITY.name();
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);

		try {
			CcpJsonRepresentation error = crud.getOneById(entityName, errorId);
			this.moveToTwin(error, errorId);
			return json;
		} catch (CcpErrorBulkEntityRecordNotFound e) {
			return json;
		}
	}

	/**
	 * Creates the error in the twin and deletes it from the main entity, both with the id of the command.
	 * @param error the error read from the main entity
	 * @param errorId the id of the document
	 */
	private void moveToTwin(CcpJsonRepresentation error, String errorId) {
		CcpEntity mainEntity = JnEntityJobsnowError.ENTITY;
		CcpEntity twinEntity = mainEntity.getTwinEntity();
		CcpEntityMetaData entityMetaData = mainEntity.getEntityMetaData();
		CcpJsonRepresentation errorFields = entityMetaData.getOnlyExistingFields(error);
		CcpBulkItem createInTheTwin = new CcpBulkItem(errorFields, CcpBulkEntityOperationType.create, twinEntity, errorId);
		CcpBulkItem deleteFromTheMain = new CcpBulkItem(errorFields, CcpBulkEntityOperationType.delete, mainEntity, errorId);
		List<CcpBulkItem> items = Arrays.asList(createInTheTwin, deleteFromTheMain);
		JnExecuteBulkOperation.INSTANCE.executeBulk(items, JnDeleteKeysFromCache.INSTANCE);
	}
}
