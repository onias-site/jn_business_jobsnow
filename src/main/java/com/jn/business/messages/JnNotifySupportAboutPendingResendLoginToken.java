package com.jn.business.messages;

import com.jn.entities.JnEntityUserRequest;

public class JnNotifySupportAboutPendingResendLoginToken extends SupportInstantMessengerNotification{
	
	public static final JnNotifySupportAboutPendingResendLoginToken INSTANCE = new JnNotifySupportAboutPendingResendLoginToken();
			
	private JnNotifySupportAboutPendingResendLoginToken() {
		super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
	}
}
