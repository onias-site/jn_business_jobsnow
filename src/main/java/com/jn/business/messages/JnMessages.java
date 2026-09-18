package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Catálogo dos templates de mensagem enviados ao usuário e ao suporte. O nome de cada classe é usado
 * como tópico em {@code @JnEntitySendMessageToUserWhenWriteOperation} e em
 * {@code @JnEntitySendMessageToUserWhenTransferOperation}.
 *
 * <p>Cada template é um {@code CcpBusiness}: antes de compor e enviar a mensagem,
 * {@code JnSendMessageToUser.apply} instancia o tópico por reflexão e executa o seu {@code apply},
 * dando a cada template a chance de preparar o JSON que alimentará o corpo da mensagem. Quem não
 * precisa preparar nada devolve o JSON intacto.
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
	 * Mensagem que entrega o token de login ao usuário. Os transformadores de campo guardam os valores
	 * em claro em {@code originalEmail} e {@code originalToken} e deixam o hash em {@code email} e
	 * {@code token}, então aqui os campos são renomeados de volta: o corpo da mensagem precisa do
	 * e-mail e do token legíveis, não dos hashes.
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
