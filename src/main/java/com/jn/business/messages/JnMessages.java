package com.jn.business.messages;

import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntityUserRequest;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Agrupa templates de notificação ao suporte para situações específicas de solicitação
 * de token. Cada classe interna estende JnBusinessSendMessage associando-a à entidade
 * de bloqueio de reenvio correspondente.
 */
public class JnMessages {
	/**
	 * Notifica o suporte sobre uma solicitação pendente de reenvio de token de login.
	 * Usa JnEntityLoginTokenRequestResend.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingResendLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingResendLoginToken() {
			super(JnEntityUserRequest.ENTITY);
		}
	}
	/**
	 * Notifica o suporte sobre uma solicitação pendente de desbloqueio de token de login.
	 * Usa JnEntityLoginTokenRequestUnlock.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingUnlockLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingUnlockLoginToken() {
			super(JnEntityUserRequest.ENTITY);
		}

		public Class<?> getJsonValidationClass() {
			return Fields.class;
		}

		private static enum Fields{
			@CcpJsonCopyFieldValidationsFrom(JnEntityLoginTokenRequestUnlock.Fields.class)
			@CcpJsonFieldValidatorRequired
			password,
			@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
			@CcpJsonFieldValidatorRequired
			token,
			@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
			@CcpJsonFieldValidatorRequired
			email,
		}
	}

}
