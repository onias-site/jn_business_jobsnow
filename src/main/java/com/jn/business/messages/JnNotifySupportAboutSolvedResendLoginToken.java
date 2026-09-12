package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class JnNotifySupportAboutSolvedResendLoginToken extends SupportInstantMessengerNotification{
	protected JnNotifySupportAboutSolvedResendLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
