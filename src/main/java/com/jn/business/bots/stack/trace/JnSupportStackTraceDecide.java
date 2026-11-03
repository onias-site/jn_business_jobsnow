package com.jn.business.bots.stack.trace;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * Second step of the {@code solveStackTrace} command: the operator tells that the error was solved, closing the ticket
 * ({@code ticketSolved}; the bot deletes it when the session ends), or leaves it open to go to the next ticket
 * ({@code ticketSkipped}, with {@link JnJsonInstantMessengerFields#skipTheTicket}). The record of the error stays, as
 * the history of what happened. Any other answer is asked again ({@code invalidAnswer}).
 */
public class JnSupportStackTraceDecide implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JnSupportStackTraceDecide INSTANCE = new JnSupportStackTraceDecide();

	private JnSupportStackTraceDecide() {}

	/** The answers of the operator. */
	enum Answers implements CcpJsonFieldName {
		/** The error was solved. */
		solved { public String getValue() { return "1"; } },
		/** Goes to the next ticket. */
		next { public String getValue() { return "2"; } }
	}

	/**
	 * Handles the answer of the operator.
	 * @param json the session, with {@code errorId} and the answer
	 * @return never returns normally
	 * @throws CcpErrorFlowDisturb with {@code ticketSolved}, {@code ticketSkipped} or {@code invalidAnswer}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String typedValue = json.getAsString(JnJsonCommonsFields.typedValue);
		String answer = typedValue.trim();
		String solvedAnswer = Answers.solved.getValue();
		boolean solved = solvedAnswer.equals(answer);

		if(solved) {
			CcpJsonRepresentation jsonWithReply = JnSupportStackTraceConversation.replyAboutTheError(json, JnSupportStackTraceMessages.ticketSolved);
			return JnSupportStackTraceStatus.ticketSolved.throwException(jsonWithReply);
		}

		String nextAnswer = Answers.next.getValue();
		boolean next = nextAnswer.equals(answer);

		if(next) {
			CcpJsonRepresentation jsonWithReply = JnSupportStackTraceConversation.replyAboutTheError(json, JnSupportStackTraceMessages.ticketSkipped);
			CcpJsonRepresentation jsonSkippingTheTicket = jsonWithReply.put(JnJsonInstantMessengerFields.skipTheTicket, true);
			return JnSupportStackTraceStatus.ticketSkipped.throwException(jsonSkippingTheTicket);
		}

		JnLanguage language = JnSupportStackTraceConversation.getLanguage(json);
		String options = JnSupportStackTraceMessages.options.getMessage(language);
		CcpJsonRepresentation parameters = CcpOtherConstants.EMPTY_JSON.put(JnSupportStackTraceFields.options, options);
		CcpJsonRepresentation jsonAskingAgain = JnSupportStackTraceConversation.reply(json, JnSupportStackTraceMessages.notUnderstood, parameters);
		return JnSupportStackTraceStatus.invalidAnswer.throwException(jsonAskingAgain);
	}
}
