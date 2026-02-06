package com.jn.mensageria;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.business.CcpBusiness;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAsyncTask;

public class JnFunctionMensageriaSender implements CcpBusiness {
	
	enum JsonFieldNames implements CcpJsonFieldName{
	}
	
	private final CcpMensageriaSender mensageriaSender = CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
	
	private final Class<?> jsonValidationClass;
	private final String operation;
	private final String topic;
	
	public JnFunctionMensageriaSender(CcpBusiness topic) {
		this.jsonValidationClass = topic.getJsonValidationClass();
		this.topic = topic.getClass().getName();
		this.operation = "";
	}

	protected JnFunctionMensageriaSender(CcpEntity entity, CcpEntityOperationType operation) {
		this.jsonValidationClass = operation.getJsonValidationClass(entity);
		Class<?> configurationClass = entity.getConfigurationClass();
		this.topic = configurationClass.getName();
		this.operation = operation.name();
	}

	public Map<String, Object> apply(Map<String, Object> map) {
		CcpJsonRepresentation json = new CcpJsonRepresentation(map);
		CcpJsonRepresentation response = this.apply(json);
		return response.content;
	} 
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation put = json.put(JnEntityAsyncTask.Fields.topic, this.topic); 
		
		CcpJsonRepresentation messageDetails = this.getMessageDetails(put); 
		
		JnEntityAsyncTask.ENTITY.save(messageDetails);
		CcpJsonRepresentation put2 = messageDetails.put(CcpMensageriaReceiver.Fields.mensageriaReceiver, JnMensageriaReceiver.class.getName());
		this.mensageriaSender.sendToMensageria(this.topic, this.jsonValidationClass, put2);

		return messageDetails;
	}
	
	public String toString() {
		return this.topic.getClass().getName();
	}
	
	private CcpJsonRepresentation getMessageDetails(CcpJsonRepresentation json) {
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();
		String formattedCurrentDateTime = ccpTimeDecorator.getFormattedDateTime(CcpEntityExpurgableOptions.second.format);
		
		CcpJsonRepresentation messageDetails = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAsyncTask.Fields.started, System.currentTimeMillis())
				.put(JnEntityAsyncTask.Fields.data, formattedCurrentDateTime)
				.put(JnEntityAsyncTask.Fields.messageId, UUID.randomUUID())
				.put(JnEntityAsyncTask.Fields.request, json.asPrettyJson())
				.put(JnEntityAsyncTask.Fields.operation, this.operation)
				.put(JnEntityAsyncTask.Fields.topic, this.topic)
				.mergeWithAnotherJson(json)
				;
		return messageDetails;
	}
	
	private CcpBulkItem toBulkItem(CcpEntity entity, CcpJsonRepresentation json) {
		String asyncTaskId = entity.calculateId(json);
		CcpBulkItem bulkItem = new CcpBulkItem(json, CcpBulkEntityOperationType.create, entity, asyncTaskId);
		return bulkItem;
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
		List<CcpJsonRepresentation> msgs = Arrays.asList(messages).stream().map(json -> this.getMessageDetails(json)).collect(Collectors.toList());
		List<CcpBulkItem> bulkItems = msgs.stream().filter(x -> this.canSave(x)).map(msg -> this.toBulkItem(entity, msg)).collect(Collectors.toList());
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems);
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
