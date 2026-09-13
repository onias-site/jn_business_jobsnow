package com.jn.business.messages;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpContentType;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.messages.JnAddDefaultStep;
import com.jn.messages.JnSendMessageToUser;
import com.jn.utils.JnSystemProperties;

public enum JnMessageType implements CcpHttpApiExecutor{
	email{

		public JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler) {
			JnAddDefaultStep defaultProcess = sender.addDefaultProcessToEmailSending(exceptionHandler);
			return defaultProcess;
		}
		
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
			String[] recipients = json.getAsStringArray(JnJsonCommonsFields.email, EmailApiFields.emails);
			emailSender.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
			JnEntityEmailMessageSent.ENTITY.save(json);
			return json;
		}

	},
	instantMessenger{

		public JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler) {
			JnAddDefaultStep defaultProcess = sender.addDefaultStepToInstantMessageSending(exceptionHandler);
			return defaultProcess;
		}
		
		public Class<?> getJsonValidationClass() {
			return InstantMessengerJsonValidator.class;
		}
		/**
		 * Obtém o token do bot via JnSystemProperties, determina o tipo de mensagem, tenta
		 * enviar e trata exceções de rate-limit e bloqueio de bot.
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);

			CcpJsonFieldName botName = asStringDecorator.jsonFieldName();
			
			String botToken =  JnSystemProperties.INSTANCE.getSystemInnerProperty(InstantMessengerApiFields.bots, botName);
			
			CcpJsonRepresentation jsonWithBotToken = json.put(JnJsonInstantMessengerFields.botToken, botToken);
			String messageType = jsonWithBotToken.getAsString(JnJsonInstantMessengerFields.instantMessageType);
			JnInstantMessageType instantMessenger = JnInstantMessageType.valueOf(messageType);
			
			try {
				CcpJsonRepresentation instantMessengerData = instantMessenger.execute(jsonWithBotToken);
				CcpJsonRepresentation instantMessageSent = jsonWithBotToken.mergeWithAnotherJson(instantMessengerData);
				JnEntityInstantMessengerMessageSent.ENTITY.save(instantMessageSent);
				return jsonWithBotToken;
			} catch (CcpHttpTooManyRequests e) {
				CcpJsonRepresentation retryToSendMessage = this.retryToSendMessage(jsonWithBotToken);
				return retryToSendMessage;
				
			} catch(CcpErrorInstantMessageThisBotWasBlockedByThisUser e) {
				CcpJsonRepresentation saveBlockedBot = this.saveBlockedBot(jsonWithBotToken, e.botName);
				return saveBlockedBot;
			}
		}

		private CcpJsonRepresentation retryToSendMessage(CcpJsonRepresentation json) {
			
			Integer maxTriesToSendMessage = this.getMaxTries();
			Integer triesToSendMessage = json.getOrDefault(InstantMessengerApiFields.triesToSendMessage, () -> 1);
			boolean triesToSendMessageMaiorOuIgual = triesToSendMessage >= maxTriesToSendMessage;

			if(triesToSendMessageMaiorOuIgual) {
				JnErrorUnableToSendInstantMessage jnErrorUnableToSendInstantMessage = new JnErrorUnableToSendInstantMessage(json);
				throw jnErrorUnableToSendInstantMessage;
			}
			
			Integer sleepToSendMessage = this.getSleepTimeToRetry();
			CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();

			ccpTimeDecorator.sleep(sleepToSendMessage);
			int triesToSendMessageMais = triesToSendMessage + 1;
			CcpJsonRepresentation put = json.put(InstantMessengerApiFields.triesToSendMessage, triesToSendMessageMais);
			CcpJsonRepresentation apply = this.execute(put);
			return apply;
		}

		private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
			CcpJsonRepresentation put2 = putAll.put(JnJsonInstantMessengerFields.botName, token);
			JnEntityInstantMessengerBotLocked.ENTITY.save(put2);
			return putAll;
		}

	}
	;

	public static enum EmailApiFields implements CcpJsonFieldName{
		
		emails, 
	}

	
	public static enum InstantMessengerApiFields implements CcpJsonFieldName{
		maxTriesToSendMessage, 
		triesToSendMessage, 
		sleepToSendMessage, 
		bots, 
		replyTo, 
	}
	
	public static enum JnBotType implements CcpJsonFieldName{
		support,
		user,
	}
	

	private static enum InstantMessengerJsonValidator implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType, 
	}

	@SuppressWarnings("serial")
	public static class JnErrorUnableToSendInstantMessage extends RuntimeException {
		private JnErrorUnableToSendInstantMessage(CcpJsonRepresentation json) {
			super("This message couldn't be sent. Details: " + json);
		}
	}

	public abstract JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler);
}
