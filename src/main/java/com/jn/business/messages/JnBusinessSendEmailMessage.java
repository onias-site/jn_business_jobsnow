package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpContentType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.utils.JnSystemProperties;


/**
 * Envia um email usando o provedor configurado via CcpEmailSender (ex: SendGrid).
 * Extrai do JSON os parâmetros de envio (token, URL, templateId, remetente, assunto,
 * corpo com resolução de template, tipo de conteúdo, destinatários) e registra o
 * envio em JnEntityEmailMessageSent.
 */
public class JnBusinessSendEmailMessage implements CcpHttpApiExecutor{
		
	public static enum Fields implements CcpJsonFieldName{
		email,
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
		String templateId = json.getAsString(JnEntityEmailTemplateMessage.Fields.templateId);
		String sender = json.getAsString(JnEntityEmailParametersToSend.Fields.sender);
		String subject = json.getAsString(JnEntityEmailTemplateMessage.Fields.subject);
		String message = json.getAsStringDecorator(JnEntityEmailTemplateMessage.Fields.message).text().resolveTemplate(json).content;
		CcpHttpContentType contentType = json.getAsEnum(JnEntityEmailParametersToSend.Fields.contentType, CcpHttpContentType.class, CcpHttpContentType.TEXT_HTML);
		String[] recipients = json.getAsStringArray(Fields.email, Fields.emails);
		emailSender.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
		JnEntityEmailMessageSent.ENTITY.save(json);
		return json;
	}

}
