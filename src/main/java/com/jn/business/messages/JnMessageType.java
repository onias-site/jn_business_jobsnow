package com.jn.business.messages;

import com.jn.messages.JnAddDefaultStep;
import com.jn.messages.JnSendMessageToUser;

public enum JnMessageType {
	email{

		JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler) {
			JnAddDefaultStep defaultProcess = sender.addDefaultProcessToEmailSending(exceptionHandler);
			return defaultProcess;
		}
	},
	instantMessenger{

		JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler) {
			JnAddDefaultStep defaultProcess = sender.addDefaultStepToInstantMessageSending(exceptionHandler);
			return defaultProcess;
		}
	}
	;

	abstract JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler);
}
