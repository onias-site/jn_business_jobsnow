package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class NotifySupportAboutSolvedLockedLoginToken extends SupportInstantMessengerNotification{
	protected NotifySupportAboutSolvedLockedLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
