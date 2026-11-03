package com.jn.business.bots.stack.trace;

import com.jn.messages.JnSystemMessage;

/**
 * Texts sent to the support bot operator along the {@code solveStackTrace} command, stored in
 * {@code JnEntitySystemMessage} and seeded by {@code JnEntityJobsnowError} (through
 * {@code JnSupportStackTraceBotConfiguration}).
 */
public enum JnSupportStackTraceMessages implements JnSystemMessage {

	/** Shows the error: {@code {origin}}, {@code {type}}, {@code {message}}, {@code {stackTrace}}, {@code {cause}} and {@code {options}}. */
	ticket,

	/** The options offered to the operator. */
	options,

	/** Answer that is neither of the options: {@code {options}}. */
	notUnderstood,

	/** The error was solved: {@code {errorId}}. */
	ticketSolved,

	/** The ticket stays open: {@code {errorId}}. */
	ticketSkipped,

	/** There is no error with the id: {@code {errorId}}. */
	errorNotFound,
	;
}
