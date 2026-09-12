package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnSendMessageToUser;
import com.jn.messages.JnSoWithAllAddedStepsAnd;
import com.jn.messages.JnWithTheTemplateId;
import com.jn.messages.JnAddDefaultStep;
import com.jn.messages.JnAndWithTheEntityToBlockMessageResend;
import com.jn.messages.JnAndWithTheJsonValues;
import com.jn.messages.JnAndWithTheSupportLanguage;

/**
 * Classe base para envio de mensagens que combina envio por email e por mensagem
 * instantânea usando o builder fluent JnSendMessageToUser. O templateId é o nome
 * da classe concreta que a estende; a entidade de bloqueio de reenvio é fornecida
 * pelo construtor.
 */
public abstract class JnBusinessSendMessage implements CcpBusiness{
	
	public final JnMessageSenderExceptionHandler exceptionHandler; 
	public final CcpEntity entity;
	
	protected JnBusinessSendMessage(CcpEntity entity, JnMessageSenderExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
		this.entity = entity;
	}

	/**
	 * Cria um JnSendMessageToUser, configura os passos padrão de email e mensagem
	 * instantânea, e aciona o envio com o templateId, entidade de bloqueio, valores
	 * do JSON e idioma.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String supportLanguage = json.getAsString(JnJsonCommonsFields.language);
		var clazz = this.getClass();

		String topic = clazz.getName();
		
		JnSendMessageToUser sender = new JnSendMessageToUser();
		
		JnMessageType[] messageTypes = this.getMessageTypes();
		
		JnAddDefaultStep addDefaultProcessToSendMessage = new JnAddDefaultStep(sender);
		
		for (JnMessageType messageType : messageTypes) {
			addDefaultProcessToSendMessage = messageType.addDefaultProcessToSendMessage(sender, this.exceptionHandler);
			sender = addDefaultProcessToSendMessage.and();
		}
		
		
		JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd = addDefaultProcessToSendMessage
		.soWithAllAddedProcessAnd();
		JnWithTheTemplateId withTheTemplateEntity = soWithAllAddedProcessAnd
		.withTheTemplateEntity(topic);
		JnAndWithTheEntityToBlockMessageResend andWithTheEntityToBlockMessageResend = withTheTemplateEntity
		.andWithTheEntityToBlockMessageResend(this.entity);
		CcpJsonRepresentation put2 = json.put(JnJsonCommonsFields.subjectType, topic);
		JnAndWithTheJsonValues andWithTheMessageValuesFromJson = andWithTheEntityToBlockMessageResend
		.andWithTheMessageValuesFromJson(put2);
		JnAndWithTheSupportLanguage andWithTheSupportLanguage = andWithTheMessageValuesFromJson
		.andWithTheSupportLanguage(supportLanguage);

		CcpJsonRepresentation result = andWithTheSupportLanguage
		.sendAllMessages() 
		;

		CcpJsonRepresentation put = result.put(JnJsonCommonsFields.subjectType, topic);
		return put;
	}
	
	public abstract JnMessageType[] getMessageTypes();

}
