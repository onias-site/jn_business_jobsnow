package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnSendMessageToUser;

/**
 * Classe base para envio de mensagens que combina envio por email e por mensagem
 * instantânea usando o builder fluent JnSendMessageToUser. O templateId é o nome
 * da classe concreta que a estende; a entidade de bloqueio de reenvio é fornecida
 * pelo construtor.
 */
public class JnBusinessSendMessage implements CcpBusiness{
	
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
	
		String topic = this.getClass().getName();
		
		JnSendMessageToUser sender = new JnSendMessageToUser();

		CcpJsonRepresentation result = sender
		.addDefaultProcessToEmailSending(this.exceptionHandler)
		.and()
		.addDefaultStepToInstantMessageSending(this.exceptionHandler)
		.soWithAllAddedProcessAnd()
		.withTheTemplateEntity(topic)
		.andWithTheEntityToBlockMessageResend(this.entity)
		.andWithTheMessageValuesFromJson(json.put(JnJsonCommonsFields.subjectType, topic))
		.andWithTheSupportLanguage(supportLanguage)
		.sendAllMessages() 
		;

		CcpJsonRepresentation put = result.put(JnJsonCommonsFields.subjectType, topic);
		return put;
	}

}
