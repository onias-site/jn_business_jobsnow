package com.jn.business.messages;

import com.jn.entities.JnEntityJobsnowWarning;

public class JnBusinessNotifyWarning extends SupportInstantMessengerNotification{
	
	public static final JnBusinessNotifyWarning instance = new JnBusinessNotifyWarning();
	
	private JnBusinessNotifyWarning() {
		super(JnEntityJobsnowWarning.ENTITY, JnMessageSenderExceptionHandler.LOG);
	}
	
}
