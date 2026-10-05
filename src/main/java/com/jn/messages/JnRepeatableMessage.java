package com.jn.messages;

/**
 * Marks a message template whose notice must go out every time it is triggered, even when the same notice
 * (same subject to the same email, same text to the same chat) was already sent within the window of
 * {@code jn_email_message_sent} (daily) or {@code jn_instant_messenger_message_sent} (hourly).
 *
 * <p>The "already sent" check exists so that a flow repeated by mistake does not spam the recipient. It does
 * not fit a notice that tells about a new fact each time, such as a new skill hierarchy fix request: up to
 * 2026-09-30 the second request of the same user in the same day was refused as a repetition, and since that
 * notice fails with an error, the request was saved but answered with 500 and the support was never told.
 * The other checks (parameters, template, blocked recipient) still apply.
 */
public interface JnRepeatableMessage {

	/**
	 * Whether the template id (the name of the class that prepares the message) is a repeatable message. A template id that
	 * is not a class name is not repeatable.
	 * @param templateId the template id
	 * @return {@code true} for a repeatable message
	 */
	static boolean isRepeatable(String templateId) {
		try {
			Class<?> template = Class.forName(templateId);
			boolean repeatable = JnRepeatableMessage.class.isAssignableFrom(template);
			return repeatable;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
