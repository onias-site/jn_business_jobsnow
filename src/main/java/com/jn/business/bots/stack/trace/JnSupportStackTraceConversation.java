package com.jn.business.bots.stack.trace;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * Texts sent to the support bot operator along the {@code solveStackTrace} command, in the language of the session.
 */
final class JnSupportStackTraceConversation {

	/** Utility class; not instantiable. */
	private JnSupportStackTraceConversation() {}

	/**
	 * Returns the language of the session, Portuguese by default.
	 * @param json the session
	 * @return the language
	 */
	static JnLanguage getLanguage(CcpJsonRepresentation json) {
		String languageName = json.getAsString(JnJsonCommonsFields.language);
		boolean noLanguage = languageName.isEmpty();
		JnLanguage language = noLanguage ? JnLanguage.portuguese : JnLanguage.valueOf(languageName);
		return language;
	}

	/**
	 * Puts in the session the text of the item, in the language of the session, as the reply to the operator.
	 * @param json the session
	 * @param message the text
	 * @param parameters the values of the template
	 * @return the session with the reply
	 */
	static CcpJsonRepresentation reply(CcpJsonRepresentation json, JnSupportStackTraceMessages message, CcpJsonRepresentation parameters) {
		JnLanguage language = getLanguage(json);
		String text = message.getMessage(language, parameters);
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, text);
		return jsonWithReply;
	}

	/**
	 * Puts in the session the text about the error of the session as the reply to the operator.
	 * @param json the session
	 * @param message the text
	 * @return the session with the reply
	 */
	static CcpJsonRepresentation replyAboutTheError(CcpJsonRepresentation json, JnSupportStackTraceMessages message) {
		CcpJsonRepresentation parameters = json.getJsonPiece(JnSupportStackTraceFields.errorId);
		CcpJsonRepresentation jsonWithReply = reply(json, message, parameters);
		return jsonWithReply;
	}
}
