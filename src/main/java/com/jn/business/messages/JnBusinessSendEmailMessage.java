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


public class JnBusinessSendEmailMessage implements CcpHttpApiExecutor{
	//TODO JSON VALIDATIONS	
	public static enum Fields implements CcpJsonFieldName{
		email,
		emails
		;
	} 
	
	
	public static final JnBusinessSendEmailMessage INSTANCE = new JnBusinessSendEmailMessage(); 
	
	private JnBusinessSendEmailMessage() {	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) { 
		
		CcpEmailSender emailSender = CcpDependencyInjection.getDependency(CcpEmailSender.class);
		
		JnSystemProperties jsp = new JnSystemProperties();
		
		String providerToken = jsp.tokenEmailValue();
		String providerUrl = jsp.urlEmailValue();
		String templateId = json.getAsString(JnEntityEmailTemplateMessage.Fields.templateId);
		String sender = json.getAsString(JnEntityEmailParametersToSend.Fields.sender);
		String subject = json.getAsString(JnEntityEmailTemplateMessage.Fields.subject);
		String message = json.getAsStringDecorator(JnEntityEmailTemplateMessage.Fields.message).text().resolveTemplate(json).content;
		CcpHttpContentType contentType = json.getAsEnum(JnEntityEmailParametersToSend.Fields.contentType, CcpHttpContentType.class);
		String[] recipients = json.getAsStringArray(Fields.email, Fields.emails);
		emailSender.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
		JnEntityEmailMessageSent.ENTITY.save(json);
		return json;
	}

}
