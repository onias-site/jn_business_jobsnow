package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnBotType;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.exceptions.JnErrorSupportLanguageIsMissing;
import com.jn.messages.JnSendMessageToUser;
import com.jn.utils.JnSystemProperties;


/**
 * Classe central de notificação ao suporte. Lê a propriedade supportLanguage do
 * application_properties, lança JnErrorSupportLanguageIsMissing se ausente, e usa
 * o builder fluent JnSendMessageToUser para enviar email e mensagem instantânea ao
 * suporte. Após o envio, salva o resultado em JnEntityJobsnowPenddingError.
 */
public class JnBusinessNotifySupport {
	enum JsonFieldNames implements CcpJsonFieldName{
		supportLanguage, msg
	}
	

	public static final JnBusinessNotifySupport INSTANCE = new JnBusinessNotifySupport();
	
	private JnBusinessNotifySupport() {
		
	}
	
	/**
	 * Configura e executa o envio de notificação ao suporte, garantindo a presença
	 * do idioma configurado e registrando o erro pendente.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json, String topic, CcpEntity entityToSaveError, JnSendMessageToUser sender) {

		String supportLanguage =  JnSystemProperties.INSTANCE.supportLanguage();
		
		boolean hasNotLanguage = supportLanguage.trim().isEmpty();
		
		if(hasNotLanguage) {
			throw new JnErrorSupportLanguageIsMissing();
		}
		CcpJsonRepresentation duplicateValueFromField = json
				.put(JnEntityInstantMessengerMessageSent.Fields.botName, JnBotType.support)
				//DOUBT PRECISA MESMO DESSA LINHA ABAIXO?
				.duplicateValueFromField(JnEntityJobsnowError.Fields.message, JsonFieldNames.msg);
		CcpJsonRepresentation result = sender
		.addDefaultProcessToEmailSending()
		.and()
		.addDefaultStepToInstantMessageSending()
		.soWithAllAddedProcessAnd()
		.withTheTemplateEntity(topic)
		.andWithTheEntityToBlockMessageResend(entityToSaveError)
		.andWithTheMessageValuesFromJson(duplicateValueFromField)
		.andWithTheSupportLanguage(supportLanguage)
		.sendAllMessages();
		
		JnEntityJobsnowPenddingError.ENTITY.save(result);

		return json;
	}

}
