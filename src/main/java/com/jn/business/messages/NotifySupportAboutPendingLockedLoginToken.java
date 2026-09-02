package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class NotifySupportAboutPendingLockedLoginToken extends SupportInstantMessengerNotification{
	protected NotifySupportAboutPendingLockedLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}
	

}
