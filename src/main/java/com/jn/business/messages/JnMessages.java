package com.jn.business.messages;

import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;

/**
 * Agrupa templates de notificação ao suporte para situações específicas de solicitação
 * de token. Cada classe interna estende JnBusinessSendMessage associando-a à entidade
 * de bloqueio de reenvio correspondente.
 */
public class JnMessages {
	//FIXME FALTANDO TEMPLATE
	/**
	 * Notifica o suporte sobre uma solicitação pendente de reenvio de token de login.
	 * Usa JnEntityLoginTokenRequestResend.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingResendLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingResendLoginToken() {
			super(JnEntityLoginTokenRequestResend.ENTITY);
		}
	}
	//FIXME FALTANDO TEMPLATE
	/**
	 * Notifica o suporte sobre uma solicitação pendente de desbloqueio de token de login.
	 * Usa JnEntityLoginTokenRequestUnlock.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingUnlockLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingUnlockLoginToken() {
			super(JnEntityLoginTokenRequestUnlock.ENTITY);
		}
	}

}
