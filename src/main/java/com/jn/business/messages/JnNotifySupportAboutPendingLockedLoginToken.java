package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class JnNotifySupportAboutPendingLockedLoginToken extends SupportInstantMessengerNotification{

	public static final JnNotifySupportAboutPendingLockedLoginToken INSTANCE = new JnNotifySupportAboutPendingLockedLoginToken();
	
	private  JnNotifySupportAboutPendingLockedLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}
	

}
