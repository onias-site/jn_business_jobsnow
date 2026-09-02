package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class NotifySupportAboutSolvedResendLoginToken extends SupportInstantMessengerNotification{
	protected NotifySupportAboutSolvedResendLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
