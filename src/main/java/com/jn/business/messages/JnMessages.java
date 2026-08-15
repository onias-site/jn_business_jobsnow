package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnBotType;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntityUserRequest;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnSystemProperties;

/**
 * Agrupa templates de notificação ao suporte para situações específicas de solicitação
 * de token. Cada classe interna estende JnBusinessSendMessage associando-a à entidade
 * de bloqueio de reenvio correspondente.
 */
public class JnMessages {
	
	
	private static class SupportInstantMessengerNotification extends JnBusinessSendMessage{

		protected SupportInstantMessengerNotification(CcpEntity entity, JnMessageSenderExceptionHandler exceptionHandler) {
			super(entity, exceptionHandler);
		}
		public Class<?> getJsonValidationClass() {
			return Fields.class;
		}

		private static enum Fields{
			@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
			@CcpJsonFieldValidatorRequired
			email,
		}
		
			
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			String supportLanguage =  JnSystemProperties.INSTANCE.supportLanguage();
		
			String templateId = this.getClass().getName();
			
			CcpJsonRepresentation put = json
					.put(JnEntityInstantMessengerParametersToSend.Fields.botName, JnBotType.support)
					.put(JnEntityInstantMessengerParametersToSend.Fields.templateId, templateId)
					.put(JnEntityEmailTemplateMessage.Fields.language, supportLanguage)
					;
			
			CcpJsonRepresentation apply = super.apply(put);
			
			return apply;
		}
	}
	
	/**
	 * Notifica o suporte sobre uma solicitação pendente de reenvio de token de login.
	 * Usa JnEntityLoginTokenRequestResend.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingResendLoginToken extends SupportInstantMessengerNotification{
		protected NotifySupportAboutPendingResendLoginToken() {
			super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
		}
	}
	/**
	 * Notifica o suporte sobre uma solicitação pendente de desbloqueio de token de login.
	 * Usa JnEntityLoginTokenRequestUnlock.ENTITY como entidade de bloqueio.
	 */
	public static class NotifySupportAboutPendingUnlockLoginToken extends SupportInstantMessengerNotification{
		protected NotifySupportAboutPendingUnlockLoginToken() {
			super(JnEntityUserRequest.ENTITY, JnMessageSenderExceptionHandler.THROWS);
		}

		public Class<?> getJsonValidationClass() {
			return Fields.class;
		}
		
		private static enum Fields{
			@CcpJsonCopyFieldValidationsFrom(JnEntityLoginTokenRequestUnlock.Fields.class)
			@CcpJsonFieldValidatorRequired
			password,
			@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
			@CcpJsonFieldValidatorRequired
			token,
			@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
			@CcpJsonFieldValidatorRequired
			email,
		}
	}

	
	
	/**
	 * Classe central de notificação ao suporte. Lê a propriedade supportLanguage do
	 * application_properties, lança JnErrorSupportLanguageIsMissing se ausente, e usa
	 * o builder fluent JnSendMessageToUser para enviar email e mensagem instantânea ao
	 * suporte. Após o envio, salva o resultado em JnEntityJobsnowPenddingError.
	 */
	public static class JnBusinessNotifyError extends SupportInstantMessengerNotification{
		
		public static final JnBusinessNotifyError instance = new JnBusinessNotifyError();
		
		private JnBusinessNotifyError() {
			super(JnEntityJobsnowError.ENTITY, JnMessageSenderExceptionHandler.LENIENT);
		}
		
		/**
		 * Configura e executa o envio de notificação ao suporte
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			CcpJsonRepresentation result = super.apply(json);
			JnEntityJobsnowPenddingError.ENTITY.save(result);

			return result;
		}
	}

	public static class JnBusinessNotifyWarning extends SupportInstantMessengerNotification{
		
		public static final JnBusinessNotifyWarning instance = new JnBusinessNotifyWarning();
		
		private JnBusinessNotifyWarning() {
			super(JnEntityJobsnowWarning.ENTITY, JnMessageSenderExceptionHandler.LOG);
		}
		
	}

	
	/**
	 * Envia o token de acesso ao usuário (via email e/ou mensagem instantânea). Antes de
	 * delegar ao JnBusinessSendMessage, prepara o JSON mesclando dados do request com os
	 * dados de contexto, gera o hash do token, duplica o email para o campo chatId do
	 * mensageiro instantâneo e o token para o campo botName.
	 */
	public static class JnBusinessSendUserToken extends JnBusinessSendMessage{
			
		enum JsonFieldNames implements CcpJsonFieldName{
			request, originalEmail, originalToken
		}
		
		public static final JnBusinessSendUserToken INSTANCE = new JnBusinessSendUserToken();
		
		private JnBusinessSendUserToken() {
			super(JnEntityLoginToken.ENTITY, JnMessageSenderExceptionHandler.THROWS);
		}
		
		/**
		 * Prepara o JSON com transformações de campos (hash de token, mapeamento email-chatId)
		 * e delega o envio ao método apply da superclasse.
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpJsonRepresentation request = json.getInnerJson(JsonFieldNames.request);
			CcpJsonRepresentation transformedJson = request.mergeWithAnotherJson(json)
					.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token)
					.duplicateValueFromField(JsonFieldNames.originalEmail, JnEntityLoginToken.Fields.email, 
							JnEntityInstantMessengerParametersToSend.Fields.chatId)
					.duplicateValueFromField(JsonFieldNames.originalToken, JnJsonTransformersFieldsEntityDefault.token)
					;
			CcpJsonRepresentation apply = super.apply(transformedJson);
			
			return apply;
		}

	}

}
