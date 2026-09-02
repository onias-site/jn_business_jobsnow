package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class NotifySupportAboutPendingResendLoginToken extends SupportInstantMessengerNotification{
	protected NotifySupportAboutPendingResendLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
