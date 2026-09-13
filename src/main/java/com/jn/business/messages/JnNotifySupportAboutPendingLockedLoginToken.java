package com.jn.business.messages;

public class JnNotifySupportAboutPendingLockedLoginToken extends SupportInstantMessengerNotification{

	public static final JnNotifySupportAboutPendingLockedLoginToken INSTANCE = new JnNotifySupportAboutPendingLockedLoginToken();
	
	private  JnNotifySupportAboutPendingLockedLoginToken() {
		super(JnMessageSenderExceptionHandler.THROWS);
	}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}
	

}
