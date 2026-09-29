package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Catalog of the message templates sent to the user and to support. Each class name is used
 * as the topic in {@code @JnEntitySendMessageToUserWhenWriteOperation} and in
 * {@code @JnEntitySendMessageToUserWhenTransferOperation}.
 *
 * <p>Each template is a {@code CcpBusiness}: before composing and sending the message,
 * {@code JnSendMessageToUser.apply} instantiates the topic via reflection and runs its {@code apply},
 * giving each template the chance to prepare the JSON that will feed the message body. Templates
 * that need no preparation return the JSON untouched.
 */
public class JnMessages {

	public static class JnNotifySupportAboutPendingLockedLoginToken implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	public static class JnNotifySupportAboutPendingResendLoginToken implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	public static class JnNotifySupportAboutSolvedLockedLoginToken implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	public static class JnNotifySupportAboutSolvedResendLoginToken implements CcpBusiness {
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
	public static class JnNotifyUserAboutLoginToken implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation withOriginalEmail = json
					.renameField(JnJsonTransformersFieldsEntityDefault.JsonFieldNames.originalEmail, JnJsonCommonsFields.email);
			CcpJsonRepresentation withOriginalToken = withOriginalEmail
					.renameField(JnJsonCommonsFields.originalToken, JnEntityLoginToken.Fields.token);
			return withOriginalToken;
		}
	}

	public static class JnNotifySupportAboutAnError implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	public static class JnNotifySupportAboutWaring implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}
}
