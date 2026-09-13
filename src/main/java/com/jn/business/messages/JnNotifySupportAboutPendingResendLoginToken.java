package com.jn.business.messages;

public class JnNotifySupportAboutPendingResendLoginToken extends SupportInstantMessengerNotification{
	
	public static final JnNotifySupportAboutPendingResendLoginToken INSTANCE = new JnNotifySupportAboutPendingResendLoginToken();
			
	private JnNotifySupportAboutPendingResendLoginToken() {
		super(JnMessageSenderExceptionHandler.THROWS);
	}
}
