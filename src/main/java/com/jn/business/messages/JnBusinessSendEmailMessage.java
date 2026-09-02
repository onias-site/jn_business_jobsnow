package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpContentType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnSystemProperties;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Envia um email usando o provedor configurado via CcpEmailSender (ex: SendGrid).
 * Extrai do JSON os parâmetros de envio (token, URL, templateId, remetente, assunto,
 * corpo com resolução de template, tipo de conteúdo, destinatários) e registra o
 * envio em JnEntityEmailMessageSent.
 */
public class JnBusinessSendEmailMessage implements CcpHttpApiExecutor{
		
	public static enum Fields implements CcpJsonFieldName{
		emails
		;
	} 
	
	
	public static final JnBusinessSendEmailMessage INSTANCE = new JnBusinessSendEmailMessage(); 
	
	private JnBusinessSendEmailMessage() {	}

	/**
	 * Obtém os parâmetros de email do JSON e das propriedades do sistema, resolve o
	 * template da mensagem, envia via CcpEmailSender e salva o registro de envio.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpEmailSender emailSender = CcpDependencyInjection.getDependency(CcpEmailSender.class);
		
		String providerUrl =  JnSystemProperties.INSTANCE.urlEmailValue();
		String providerToken =  JnSystemProperties.INSTANCE.tokenEmailValue();
		String templateId = json.getAsString(JnJsonCommonsFields.templateId);
		String sender = json.getAsString(JnJsonCommonsFields.sender);
		String subject = json.getAsString(JnJsonCommonsFields.subject);
		CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonCommonsFields.message);
		var asStringDecoratorText = asStringDecorator.text();
		var resolveTemplate = asStringDecoratorText.resolveTemplate(json);
		String message = resolveTemplate.content;
		CcpHttpContentType contentType = json.getAsEnum(JnJsonCommonsFields.contentType, CcpHttpContentType.class, CcpHttpContentType.TEXT_HTML);
		String[] recipients = json.getAsStringArray(JnJsonCommonsFields.email, Fields.emails);
		emailSender.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
		JnEntityEmailMessageSent.ENTITY.save(json);
		return json;
	}

}
