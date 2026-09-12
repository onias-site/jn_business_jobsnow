package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class JnNotifySupportAboutSolvedLockedLoginToken extends SupportInstantMessengerNotification{
	protected JnNotifySupportAboutSolvedLockedLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
