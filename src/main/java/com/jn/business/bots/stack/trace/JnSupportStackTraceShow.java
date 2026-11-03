package com.jn.business.bots.stack.trace;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.utils.JnLanguage;

/**
 * First step of the {@code solveStackTrace} command ({@code /solveStackTrace <errorId>}): shows the operator the error of
 * the ticket and asks for the decision.
 *
 * <p>The id of a disposable record depends on the period it was written in, and no primary key value travels in the
 * command, so the document is read directly by its id, through {@link CcpCrud}. {@code jn_jobsnow_error} keeps the
 * documents of the past periods for that: a ticket may wait longer than the record lives. An id that does not exist
 * diverts the flow with {@code errorNotFound}.
 */
public class JnSupportStackTraceShow implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JnSupportStackTraceShow INSTANCE = new JnSupportStackTraceShow();

	private JnSupportStackTraceShow() {}

	/**
	 * Shows the error.
	 * @param json the session, with {@code errorId}
	 * @return the session with the reply
	 * @throws CcpErrorFlowDisturb with {@code errorNotFound}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String errorId = json.getAsString(JnSupportStackTraceFields.errorId);
		String entityName = JnEntityJobsnowError.ENTITY.name();
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);

		try {
			CcpJsonRepresentation error = crud.getOneById(entityName, errorId);
			CcpJsonRepresentation jsonWithReply = this.showError(json, error);
			return jsonWithReply;
		} catch (CcpErrorBulkEntityRecordNotFound e) {
			CcpJsonRepresentation jsonWithReply = JnSupportStackTraceConversation.replyAboutTheError(json, JnSupportStackTraceMessages.errorNotFound);
			return JnSupportStackTraceStatus.errorNotFound.throwException(jsonWithReply);
		}
	}

	/**
	 * Puts the error in the reply.
	 * @param json the session
	 * @param error the error
	 * @return the session with the reply
	 */
	private CcpJsonRepresentation showError(CcpJsonRepresentation json, CcpJsonRepresentation error) {
		JnLanguage language = JnSupportStackTraceConversation.getLanguage(json);
		String options = JnSupportStackTraceMessages.options.getMessage(language);
		List<String> stackTraceFrames = error.getAsStringList(JnEntityJobsnowError.Fields.stackTrace);
		String stackTraceOneFramePerLine = String.join("\n", stackTraceFrames);
		String origin = error.getAsString(JnEntityJobsnowError.Fields.origin);
		String type = error.getAsString(JnEntityJobsnowError.Fields.type);
		String message = error.getAsString(JnEntityJobsnowError.Fields.message);
		String cause = error.getAsString(JnEntityJobsnowError.Fields.cause);

		CcpJsonRepresentation parametersWithOptions = CcpOtherConstants.EMPTY_JSON.put(JnSupportStackTraceFields.options, options);
		CcpJsonRepresentation parametersWithOrigin = parametersWithOptions.put(JnEntityJobsnowError.Fields.origin, origin);
		CcpJsonRepresentation parametersWithType = parametersWithOrigin.put(JnEntityJobsnowError.Fields.type, type);
		CcpJsonRepresentation parametersWithMessage = parametersWithType.put(JnEntityJobsnowError.Fields.message, message);
		CcpJsonRepresentation parametersWithStackTrace = parametersWithMessage.put(JnEntityJobsnowError.Fields.stackTrace, stackTraceOneFramePerLine);
		CcpJsonRepresentation parameters = parametersWithStackTrace.put(JnEntityJobsnowError.Fields.cause, cause);

		CcpJsonRepresentation jsonWithReply = JnSupportStackTraceConversation.reply(json, JnSupportStackTraceMessages.ticket, parameters);
		return jsonWithReply;
	}
}
