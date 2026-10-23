package com.jn.business.messages;

import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Catalog of the message templates sent to the user and to support. Each class is a {@code JnMessageTemplate}, named by
 * the message annotations of the entities (such as
 * {@code @CcpAfterSaveFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning}), and its name is the topic of the sending.
 *
 * <p>Each template is a {@code CcpBusiness}: before composing and sending the message,
 * {@code JnSendMessageToUser.apply} instantiates the topic via reflection and runs its {@code apply},
 * giving each template the chance to prepare the JSON that will feed the message body. Templates
 * that need no preparation return the JSON untouched.
 */
public class JnMessages {

	/** Notice to the support team about a new request to unlock a login token. */
	public static class JnNotifySupportAboutPendingLockedLoginToken implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutPendingLockedLoginToken INSTANCE = new JnNotifySupportAboutPendingLockedLoginToken();

		private JnNotifySupportAboutPendingLockedLoginToken() {}

		/**
		 * Needs no preparation.
		 * @param json the values of the message
		 * @return the same JSON
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	/** Notice to the support team about a new request to resend a login token. */
	public static class JnNotifySupportAboutPendingResendLoginToken implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutPendingResendLoginToken INSTANCE = new JnNotifySupportAboutPendingResendLoginToken();

		private JnNotifySupportAboutPendingResendLoginToken() {}

		/**
		 * Needs no preparation.
		 * @param json the values of the message
		 * @return the same JSON
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	/** Notice to the support team that a request to unlock a login token was solved. */
	public static class JnNotifySupportAboutSolvedLockedLoginToken implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutSolvedLockedLoginToken INSTANCE = new JnNotifySupportAboutSolvedLockedLoginToken();

		private JnNotifySupportAboutSolvedLockedLoginToken() {}

		/**
		 * Needs no preparation.
		 * @param json the values of the message
		 * @return the same JSON
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	/** Notice to the support team that a request to resend a login token was solved. */
	public static class JnNotifySupportAboutSolvedResendLoginToken implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutSolvedResendLoginToken INSTANCE = new JnNotifySupportAboutSolvedResendLoginToken();

		private JnNotifySupportAboutSolvedResendLoginToken() {}

		/**
		 * Needs no preparation.
		 * @param json the values of the message
		 * @return the same JSON
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	/**
	 * Message that delivers the login token to the user. The field transformers keep the plain values
	 * in {@code originalEmail} and {@code originalToken} and leave the hash in {@code email} and
	 * {@code token}, so here the fields are renamed back: the message body needs the readable
	 * email and token, not the hashes.
	 */
	public static class JnNotifyUserAboutLoginToken implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifyUserAboutLoginToken INSTANCE = new JnNotifyUserAboutLoginToken();

		private JnNotifyUserAboutLoginToken() {}

		/**
		 * Renames the plain e-mail and token back to {@code email} and {@code token}.
		 * @param json the values of the message
		 * @return the values with the readable e-mail and token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation withOriginalEmail = json
					.renameField(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalEmail, JnJsonCommonsFields.email);
			CcpJsonRepresentation withOriginalToken = withOriginalEmail
					.renameField(JnJsonCommonsFields.originalToken, JnJsonCommonsFields.token);
			return withOriginalToken;
		}
	}

	/**
	 * Error notice sent to support as a file. See {@link #toSupportNotice(CcpJsonRepresentation)}.
	 */
	public static class JnNotifySupportAboutAnError implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutAnError INSTANCE = new JnNotifySupportAboutAnError();

		private JnNotifySupportAboutAnError() {}

		/**
		 * Prepares the support notice.
		 * @param json the error
		 * @return the notice
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation supportNotice = toSupportNotice(json);
			return supportNotice;
		}
	}

	/**
	 * Warning notice sent to support as a file. See {@link #toSupportNotice(CcpJsonRepresentation)}.
	 */
	public static class JnNotifySupportAboutWaring implements JnMessageTemplate {
		/** The single instance: the class keeps no state. */
		public final static JnNotifySupportAboutWaring INSTANCE = new JnNotifySupportAboutWaring();

		private JnNotifySupportAboutWaring() {}

		/**
		 * Prepares the support notice.
		 * @param json the warning
		 * @return the notice
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation supportNotice = toSupportNotice(json);
			return supportNotice;
		}
	}

	/** Fields of the support notice. */
	enum JsonFieldNames implements CcpJsonFieldName {
		/** The {@code msg} field. */
		msg
	}

	/**
	 * Prepares the error/warning JSON for the support notice template, whose {@code message} is
	 * {@code {type} ... {msg} ... {completeStackTrace} ... {cause}}.
	 * <ul>
	 * <li>The exception text goes from {@code message} to {@code msg}: the JSON of the sending prevails over the
	 * template when both are merged, so an exception text left in {@code message} took the place of the template
	 * and the file delivered to support carried only that text (the copy to {@code msg} was lost when
	 * {@code JnBusinessNotifySupport} was deleted, on 2026-08-14).</li>
	 * <li>The complete stack trace becomes one frame per line, falling back to the stack trace filtered down to the
	 * domain lines when the complete one is absent (the warnings only record the filtered one).</li>
	 * </ul>
	 */
	private static CcpJsonRepresentation toSupportNotice(CcpJsonRepresentation json) {
		CcpJsonRepresentation jsonWithMsg = json.renameField(CcpJsonRepresentation.CcpStackTraceFields.message, JsonFieldNames.msg);

		boolean hasCompleteStackTrace = jsonWithMsg.containsField(CcpJsonRepresentation.CcpStackTraceFields.completeStackTrace);
		CcpJsonFieldName stackTraceField = hasCompleteStackTrace ? CcpJsonRepresentation.CcpStackTraceFields.completeStackTrace : CcpJsonRepresentation.CcpStackTraceFields.stackTrace;
		List<String> stackTraceFrames = jsonWithMsg.getAsStringList(stackTraceField);
		String stackTraceOneFramePerLine = String.join("\n", stackTraceFrames);

		CcpJsonRepresentation supportNotice = jsonWithMsg.put(CcpJsonRepresentation.CcpStackTraceFields.completeStackTrace, stackTraceOneFramePerLine);
		return supportNotice;
	}
}
