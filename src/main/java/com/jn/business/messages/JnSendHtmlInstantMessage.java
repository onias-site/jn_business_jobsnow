package com.jn.business.messages;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.jn.business.http.JnHttpApiErrors;

/**
 * Sends an HTML message through the instant messenger, recording its HTTP failures (since 2026-11-27) in
 * {@code jn_http_api_error_client} or {@code jn_http_api_error_server} ({@link JnHttpApiErrors}): the answers of the
 * support bot to the job postings of the groups and the publishing of the positions and of the job postings in the groups.
 */
public class JnSendHtmlInstantMessage {

	private JnSendHtmlInstantMessage() {}

	/**
	 * Sends the message; an HTTP failure is recorded and thrown (a client error as permanent).
	 * @param instantMessenger the instant messenger
	 * @param botName the bot
	 * @param botToken the token of the bot
	 * @param chatId the chat
	 * @param replyTo the message answered, or 0
	 * @param message the HTML message
	 * @return the answer of the instant messenger
	 */
	public static CcpJsonRepresentation send(CcpInstantMessenger instantMessenger, CcpJsonFieldName botName, String botToken, Long chatId, Long replyTo, String message) {
		try {
			CcpJsonRepresentation result = instantMessenger.sendHtmlMessage(botName, botToken, chatId, replyTo, message);
			return result;
		} catch (CcpErrorHttp e) {
			RuntimeException recordedError = JnHttpApiErrors.record(JnHttpApiErrors.ExternalApi.telegram, e);
			throw recordedError;
		}
	}
}
