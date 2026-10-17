package com.jn.business.messages;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityMessageSenderExceptionHandler;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityMessageType;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityMessageTemplate;
import com.jn.messages.JnSendMessageToUser;

/**
 * A message template of the platform ({@code JnMessages}, {@code VisMessages}...): a {@code CcpBusiness} that prepares
 * the JSON of the message and, as the {@code CcpEntityMessageTemplate} named by the message annotations of the entities,
 * sends itself through {@code JnSendMessageToUser}, with its class name as the topic.
 */
public interface JnMessageTemplate extends CcpBusiness, CcpEntityMessageTemplate {

	/**
	 * Sends this message through {@code JnSendMessageToUser}: the topic is the name of the class, the kinds of message and
	 * the failure policy are the ones of the same name of the platform.
	 * @param json the record, which feeds the message
	 * @param exceptionHandler what happens when the sending fails
	 * @param messageTypes the kinds of message sent
	 * @return the JSON after the sending
	 */
	default CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpEntityMessageSenderExceptionHandler exceptionHandler, CcpEntityMessageType... messageTypes) {
		String exceptionHandlerName = exceptionHandler.name();
		JnMessageSenderExceptionHandler platformExceptionHandler = JnMessageSenderExceptionHandler.valueOf(exceptionHandlerName);
		Stream<CcpEntityMessageType> messageTypesStream = Arrays.stream(messageTypes);
		Stream<JnMessageType> platformMessageTypesStream = messageTypesStream.map(x -> JnMessageType.valueOf(x.name()));
		List<JnMessageType> platformMessageTypesList = platformMessageTypesStream.collect(Collectors.toList());
		JnMessageType[] platformMessageTypes = platformMessageTypesList.toArray(new JnMessageType[0]);
		Class<?> messageClass = this.getClass();
		String topic = messageClass.getName();
		JnSendMessageToUser sender = new JnSendMessageToUser();
		CcpJsonRepresentation result = sender.sendAllMessages(json, topic, platformMessageTypes, platformExceptionHandler);
		return result;
	}
}
