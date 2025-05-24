package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.CcpEntity;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.exceptions.JnSupportLanguageIsMissing;
import com.jn.messages.JnSendMessage;

public class JnBusinessNotifySupport {
	

	public static final JnBusinessNotifySupport INSTANCE = new JnBusinessNotifySupport();
	
	private JnBusinessNotifySupport() {
		
	}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json, String topic, CcpEntity entityToSaveError, JnSendMessage sender) {
		String supportLanguage = new CcpStringDecorator("application_properties").propertiesFrom().environmentVariablesOrClassLoaderOrFile().getAsString("supportLanguage");
		
		boolean hasNotLanguage = supportLanguage.trim().isEmpty();
		
		if(hasNotLanguage) {
			throw new JnSupportLanguageIsMissing();
		}

		CcpJsonRepresentation duplicateValueFromField = json.renameField(JnEntityJobsnowError.Fields.message.name(), "msg");
		CcpJsonRepresentation result = sender
		.addDefaultProcessForEmailSending()
		.and()
		.addDefaultStepForTelegramSending()
		.soWithAllAddedProcessAnd()
		.withTheTemplateEntity(topic)
		.andWithTheEntityToBlockMessageResend(entityToSaveError)
		.andWithTheMessageValuesFromJson(duplicateValueFromField)
		.andWithTheSupportLanguage(supportLanguage)
		.sendAllMessages();
		
		JnEntityJobsnowPenddingError.ENTITY.createOrUpdate(result);
		

		return json;
	}

}
