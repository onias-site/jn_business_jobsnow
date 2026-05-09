package com.jn.business.messages;

import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;

public class JnMessages {
	//FIXME FALTANDO TEMPLATE
	public static class NotifySupportAboutPendingResendLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingResendLoginToken() {
			super(JnEntityLoginTokenRequestResend.ENTITY);
		}
	}
	//FIXME FALTANDO TEMPLATE
	public static class NotifySupportAboutPendingUnlockLoginToken extends JnBusinessSendMessage{
		protected NotifySupportAboutPendingUnlockLoginToken() {
			super(JnEntityLoginTokenRequestUnlock.ENTITY);
		}
	}

}
