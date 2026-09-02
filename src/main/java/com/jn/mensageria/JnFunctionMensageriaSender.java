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
 * Responsável por enviar mensagens/tarefas para a fila de mensageria (PubSub). Cria um registro em
 * {@code JnEntityAsyncTask} com os detalhes da mensagem antes de publicar. Suporta dois modos:
 * envio de um {@code CcpBusiness} (tópico = nome da classe) ou envio de uma operação de entidade.
 */
public class JnFunctionMensageriaSender implements CcpBusiness {
	
	
	private final CcpMensageriaSender mensageriaSender = CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
	
	private final Class<?> jsonValidationClass;
	private final String entityName;
	private final String operation;
	private final String topic;
	
	public JnFunctionMensageriaSender(CcpBusiness topic) {
		this.jsonValidationClass = topic.getJsonValidationClass();
		var topicClass = topic.getClass();
		this.topic = topicClass.getName();
		this.entityName = "";
		this.operation = "";
	}

	public JnFunctionMensageriaSender(CcpEntity entity, CcpEntityOperationType operation) {
		this.jsonValidationClass = operation.getJsonValidationClass(entity);
		CcpEntityMetaData entityMetadata = entity.getEntityMetaData();
		this.topic = entityMetadata.configurationClass.getName();
		this.entityName = entityMetadata.entityName;
		this.operation = operation.name();
	}

	public Map<String, Object> apply(Map<String, Object> map) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(map);
		CcpJsonRepresentation response = this.execute(json);
		return response.content;
	} 
	
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
	
	public String toString() {
		var topicClass2 = this.topic.getClass();
		var topicClass2Name = topicClass2.getName();
		return topicClass2Name;
	}
	
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

				CcpJsonRepresentation messageDetails = put9
				.mergeWithAnotherJson(json)
				;
		return messageDetails;
	}
	
	private boolean canSave(CcpJsonRepresentation x) {
		CcpBusiness process = JnMensageriaReceiver.INSTANCE.getProcess(this.topic, x);
		if(process instanceof CcpBusiness topic) {
			boolean canSave = topic.canBeSavedAsAsyncTask();
			return canSave;
		}
		return true;
	}
	
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

	public JnFunctionMensageriaSender sendToMensageria(List<CcpJsonRepresentation> messages) {
		
		int size = messages.size();
		CcpJsonRepresentation[] a = new CcpJsonRepresentation[size];
		CcpJsonRepresentation[] array = messages.toArray(a);
		JnFunctionMensageriaSender send = this.sendToMensageria(JnEntityAsyncTask.ENTITY, array);
		return send;
	}

	public CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation... messages) {
		this.sendToMensageria(JnEntityAsyncTask.ENTITY, messages);
		return CcpOtherConstants.EMPTY_JSON;
	}
}
