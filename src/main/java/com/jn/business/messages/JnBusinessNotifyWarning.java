package com.jn.business.messages;

public class JnBusinessNotifyWarning extends SupportInstantMessengerNotification{
	
	public static final JnBusinessNotifyWarning instance = new JnBusinessNotifyWarning();
	
	private JnBusinessNotifyWarning() {
		super(JnMessageSenderExceptionHandler.LOG);
	}
	
}
