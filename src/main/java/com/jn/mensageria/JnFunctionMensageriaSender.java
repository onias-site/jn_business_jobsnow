package com.jn.mensageria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Publishes a task to messaging (Pub/Sub). The task is either a business (the topic is the name of its class) or an
 * operation on an entity (the topic is the configurator class of the entity, plus the entity name and the operation);
 * {@link JnMensageriaReceiver} runs it on the consumer side.
 */
public class JnFunctionMensageriaSender implements CcpBusiness {


	/** The messaging client. */
	private final CcpMensageriaSender mensageriaSender = CcpDependencyInjection.getDependency(CcpMensageriaSender.class);

	/** The input rules of the task. */
	private final Class<?> jsonValidationClass;
	/** The entity name, for an entity operation; empty for a business. */
	private final String entityName;
	/** The operation, for an entity operation; empty for a business. */
	private final String operation;
	/** The topic: the class name of the business or of the configurator of the entity. */
	private final String topic;

	/**
	 * Prepares the sending of a business.
	 * @param topic the business
	 */
	public JnFunctionMensageriaSender(CcpBusiness topic) {
		this.jsonValidationClass = topic.getJsonValidationClass();
		var topicClass = topic.getClass();
		this.topic = topicClass.getName();
		this.entityName = "";
		this.operation = "";
	}

	/**
	 * Prepares the sending of an entity operation.
	 * @param entity the entity
	 * @param operation the operation
	 */
	public JnFunctionMensageriaSender(CcpEntity entity, CcpEntityOperationType operation) {
		this.jsonValidationClass = operation.getJsonValidationClass(entity);
		CcpEntityMetaData entityMetadata = entity.getEntityMetaData();
		this.topic = entityMetadata.configurationClass.getName();
		this.entityName = entityMetadata.entityName;
		this.operation = operation.name();
	}

	/**
	 * Map variant of {@link #apply(CcpJsonRepresentation)}.
	 * @param map the input of the task
	 * @return the details of the message
	 */
	public Map<String, Object> apply(Map<String, Object> map) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(map);
		CcpJsonRepresentation response = this.execute(json);
		return response.content;
	}

	/**
	 * Publishes the task, naming the receiver and the entity.
	 * @param json the input of the task
	 * @return the details of the message
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation messageDetails = this.getMessageDetails(json);

		CcpJsonRepresentation messageWithReceiver = this.getMessageWithReceiver(messageDetails);
		this.mensageriaSender.sendToMensageria(this.topic, this.jsonValidationClass, messageWithReceiver);

		return messageDetails;
	}

	/**
	 * The message as published: the details plus the receiver ({@code mensageriaReceiver}) and the target entity
	 * ({@code entityName}), which the consumer needs to find who runs it. Both the single and the batch sending publish
	 * through here; up to 2026-10-07 the batch published the bare details and no message of a batch ran.
	 * @param messageDetails the details of the message
	 * @return the message to publish
	 */
	private CcpJsonRepresentation getMessageWithReceiver(CcpJsonRepresentation messageDetails) {
		String receiverName = JnMensageriaReceiver.class.getName();
		CcpJsonRepresentation messageWithReceiverName = messageDetails
				.put(CcpMensageriaReceiver.JsonFieldNames.mensageriaReceiver, receiverName);
		CcpJsonRepresentation messageWithReceiver = messageWithReceiverName
				.put(CcpMensageriaReceiver.JsonFieldNames.entityName, this.entityName);
		return messageWithReceiver;
	}

	/**
	 * Describes the task: the topic (the class of the business or of the configurator of the entity) and, for an entity
	 * operation, the operation and the entity, e.g. {@code com.jn.entities.JnEntityJobsnowError (save, jn_jobsnow_error)}.
	 * Until 2026-10-07 it returned the class of the topic text, so every sender described itself as
	 * {@code java.lang.String}.
	 * @return the description
	 */
	public String toString() {
		boolean isBusiness = this.operation.isEmpty();
		if(isBusiness) {
			return this.topic;
		}
		String description = this.topic + " (" + this.operation + ", " + this.entityName + ")";
		return description;
	}

	/**
	 * The message is the JSON plus the envelope ({@code operation} and {@code topic}), and the envelope prevails. Up to
	 * 2026-09-30 the JSON prevailed: a JSON that carried the envelope of an earlier message (a record read from the cache,
	 * which kept the whole message of the save) turned a {@code delete} into a {@code save}, so withdrawing a request
	 * saved it again instead of deleting it.
	 * @param json the input of the task
	 * @return the message
	 */
	private CcpJsonRepresentation getMessageDetails(CcpJsonRepresentation json) {
		CcpJsonRepresentation envelopeWithOperation = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.operation, this.operation);
		CcpJsonRepresentation envelope = envelopeWithOperation
				.put(CcpJsonCommonsFields.topic, this.topic);
		CcpJsonRepresentation messageDetails = json.mergeWithAnotherJson(envelope);
		return messageDetails;
	}

	/**
	 * Publishes several messages of the task in one call, each one exactly as {@link #apply} does, whatever
	 * {@link CcpBusiness#canBeSavedAsAsyncTask()} answers. Up to 2026-10-07 the batch skipped the messages of a business
	 * that answered {@code false}, while the single sending ran them.
	 * @param messages the inputs of the task
	 * @return this instance
	 */
	public JnFunctionMensageriaSender sendToMensageria(List<CcpJsonRepresentation> messages) {

		List<CcpJsonRepresentation> messagesWithReceiver = new ArrayList<>();

		for (CcpJsonRepresentation json : messages) {
			CcpJsonRepresentation messageDetails = this.getMessageDetails(json);
			CcpJsonRepresentation messageWithReceiver = this.getMessageWithReceiver(messageDetails);
			messagesWithReceiver.add(messageWithReceiver);
		}

		this.mensageriaSender.sendToMensageria(this.topic, this.jsonValidationClass, messagesWithReceiver);
		return this;
	}

	/**
	 * Publishes several messages of the task in one call.
	 * @param messages the inputs of the task
	 * @return an empty JSON
	 */
	public CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation... messages) {
		List<CcpJsonRepresentation> messageList = List.of(messages);
		this.sendToMensageria(messageList);
		return CcpOtherConstants.EMPTY_JSON;
	}
}
