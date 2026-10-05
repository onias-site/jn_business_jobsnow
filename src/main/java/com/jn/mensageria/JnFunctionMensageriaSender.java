package com.jn.mensageria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Publishes a task to messaging (Pub/Sub), after recording it in {@code jn_async_task}. The task is either a business
 * (the topic is the name of its class) or an operation on an entity (the topic is the configurator class of the entity,
 * plus the entity name and the operation); {@link JnMensageriaReceiver} runs it on the consumer side.
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
	 * Records the task in {@code jn_async_task} and publishes it, naming the receiver and the entity.
	 * @param json the input of the task
	 * @return the details of the message
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation put = json.put(JnEntityAsyncTask.Fields.topic, this.topic); 
		
		CcpJsonRepresentation messageDetails = this.getMessageDetails(put); 
		
		JnEntityAsyncTask.ENTITY.save(messageDetails);
		
		String receiverName = JnMensageriaReceiver.class.getName();
		CcpJsonRepresentation put3 = messageDetails
				.put(CcpMensageriaReceiver.JsonFieldNames.mensageriaReceiver, receiverName);
				CcpJsonRepresentation put2 = put3
				.put(CcpMensageriaReceiver.JsonFieldNames.entityName, this.entityName)
				;
		this.mensageriaSender.sendToMensageria(this.topic, this.jsonValidationClass, put2);

		return messageDetails; 
	}
	
	/**
	 * Returns the name of the class of the topic (see finding: always {@code java.lang.String}, since the topic is a text).
	 * @return the class name
	 */
	public String toString() {
		var topicClass2 = this.topic.getClass();
		var topicClass2Name = topicClass2.getName();
		return topicClass2Name;
	}
	
	/**
	 * The message is the JSON plus the envelope ({@code operation}, {@code messageId}, {@code topic}, {@code started},
	 * {@code data}, {@code request}), and the envelope prevails. Up to 2026-09-30 the JSON prevailed: a JSON that carried
	 * the envelope of an earlier message (a record read from the cache, which kept the whole message of the save) turned a
	 * {@code delete} into a {@code save} with the old {@code messageId}, so withdrawing a request saved it again instead of
	 * deleting it.
	 * @param json the input of the task
	 * @return the message
	 */
	private CcpJsonRepresentation getMessageDetails(CcpJsonRepresentation json) {
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();
		String formattedCurrentDateTime = ccpTimeDecorator.getFormattedDateTime(CcpEntityExpurgableOptions.second.format);
		long currentTimeMillis = System.currentTimeMillis();
		CcpJsonRepresentation put4 = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAsyncTask.Fields.started, currentTimeMillis);
				CcpJsonRepresentation put5 = put4
				.put(JnEntityAsyncTask.Fields.data, formattedCurrentDateTime);
				UUID randomUUID = UUID.randomUUID();
				CcpJsonRepresentation put6 = put5
				.put(JnEntityAsyncTask.Fields.messageId, randomUUID);
				String asPrettyJson = json.asPrettyJson();
				CcpJsonRepresentation put7 = put6
				.put(JnJsonCommonsFields.request, asPrettyJson);
				CcpJsonRepresentation put8 = put7
				.put(JnJsonCommonsFields.operation, this.operation);
				CcpJsonRepresentation put9 = put8
				.put(JnEntityAsyncTask.Fields.topic, this.topic);

				CcpJsonRepresentation messageDetails = json
				.mergeWithAnotherJson(put9)
				;
		return messageDetails;
	}
	
	/**
	 * Tells whether the task may be recorded as an asynchronous task.
	 * @param x the message
	 * @return what the business answers, {@code true} when it is not a business
	 */
	private boolean canSave(CcpJsonRepresentation x) {
		CcpBusiness process = JnMensageriaReceiver.INSTANCE.getProcess(this.topic, x);
		if(process instanceof CcpBusiness topic) {
			boolean canSave = topic.canBeSavedAsAsyncTask();
			return canSave;
		}
		return true;
	}
	
	/**
	 * Records the messages that may be recorded in the entity (one bulk) and publishes them.
	 * @param entity the entity of the asynchronous tasks
	 * @param messages the inputs of the task
	 * @return this instance
	 */
	private JnFunctionMensageriaSender sendToMensageria(CcpEntity entity, CcpJsonRepresentation... messages) {
		
		List<CcpBulkItem> bulkItems = new ArrayList<>();
		List<CcpJsonRepresentation> msgs = new ArrayList<>();
		
		for (CcpJsonRepresentation json : messages) {
			CcpJsonRepresentation messageDetails = this.getMessageDetails(json);
			boolean canSave2 = this.canSave(messageDetails);

			boolean canNotSave = false == canSave2;
			if(canNotSave) {
				continue;
			}
			List<CcpBulkItem> bulkItemsList = entity.toBulkItems(messageDetails, CcpBulkEntityOperationType.create);	
			bulkItems.addAll(bulkItemsList);
			msgs.add(messageDetails);
		}
		
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems, JnDeleteKeysFromCache.INSTANCE);
		this.mensageriaSender.sendToMensageria(this.topic, this.jsonValidationClass, msgs);
		return this;
	}

	/**
	 * Records and publishes several messages of the task.
	 * @param messages the inputs of the task
	 * @return this instance
	 */
	public JnFunctionMensageriaSender sendToMensageria(List<CcpJsonRepresentation> messages) {
		
		int size = messages.size();
		CcpJsonRepresentation[] a = new CcpJsonRepresentation[size];
		CcpJsonRepresentation[] array = messages.toArray(a);
		JnFunctionMensageriaSender send = this.sendToMensageria(JnEntityAsyncTask.ENTITY, array);
		return send;
	}

	/**
	 * Records and publishes several messages of the task.
	 * @param messages the inputs of the task
	 * @return an empty JSON
	 */
	public CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation... messages) {
		this.sendToMensageria(JnEntityAsyncTask.ENTITY, messages);
		return CcpOtherConstants.EMPTY_JSON;
	}
}
