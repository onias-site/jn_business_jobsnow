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

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

public enum JnMessageType implements CcpHttpApiExecutor{
	email{

		public JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler) {
			JnAddDefaultStep defaultProcess = sender.addDefaultProcessToEmailSending(exceptionHandler);
			return defaultProcess;
		}
		
		/**
		 * Gets the email parameters from the JSON and from the system properties, resolves the
		 * message template, sends it via CcpEmailSender and saves the sending record.
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			CcpEmailSender emailSender = CcpDependencyInjection.getDependency(CcpEmailSender.class);
			
			String providerUrl =  JnSystemProperties.INSTANCE.urlEmailValue();
			String providerToken =  JnSystemProperties.INSTANCE.tokenEmailValue();
			String templateId = json.getAsString(JnJsonCommonsFields.templateId);
			String sender = json.getAsString(JnJsonCommonsFields.sender);
			String subject = json.getAsString(JnJsonCommonsFields.subject);
			CcpStringDecorator messageDecorator = json.getAsStringDecorator(JnJsonCommonsFields.message);
			var messageTemplate = messageDecorator.text();
			var resolvedMessage = messageTemplate.resolveTemplate(json);
			String message = resolvedMessage.content;
			CcpHttpContentType contentType = json.getAsEnum(JnJsonCommonsFields.contentType, CcpHttpContentType.class, CcpHttpContentType.TEXT_HTML);
			String[] recipients = json.getAsStringArray(JnJsonCommonsFields.email, CcpJsonCommonsFields.emails);
			emailSender.sendSimpleTextEmailMessage(providerToken, providerUrl, templateId, sender, subject, message, contentType, recipients);
			JnEntityEmailMessageSent.ENTITY.save(json);
			return json;
		}

		/**
		 * The language is part of the email template's primary key, so without it the message is
		 * not found. The caller may provide it in the json; when it does not, the language configured
		 * for the system is used.
		 */
		public CcpJsonRepresentation getParameters(CcpJsonRepresentation json) {

			boolean languageWasInformed = json.containsField(JnJsonCommonsFields.language);

			if(languageWasInformed) {
				return json;
			}

			String systemLanguage = JnSystemProperties.INSTANCE.supportLanguage();
			CcpJsonRepresentation parameters = json.put(JnJsonCommonsFields.language, systemLanguage);
			return parameters;
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
		 * Gets the bot token via JnSystemProperties, determines the message type, tries to
		 * send it and handles rate-limit and blocked-bot exceptions.
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
			CcpStringDecorator botNameDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);

			CcpJsonFieldName botName = botNameDecorator.jsonFieldName();
			
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
				CcpJsonRepresentation retryResponse = this.retryToSendMessage(jsonWithBotToken);
				return retryResponse;
				
			} catch(CcpErrorInstantMessageThisBotWasBlockedByThisUser e) {
				CcpJsonRepresentation jsonWithBlockedBot = this.saveBlockedBot(jsonWithBotToken, e.botName);
				return jsonWithBlockedBot;
			}
		}

		private CcpJsonRepresentation retryToSendMessage(CcpJsonRepresentation json) {
			
			Integer maxTriesToSendMessage = this.getMaxTries();
			Integer triesToSendMessage = json.getOrDefault(InstantMessengerApiFields.triesToSendMessage, () -> 1);
			boolean exceededMaxTries = triesToSendMessage >= maxTriesToSendMessage;

			if(exceededMaxTries) {
				JnErrorUnableToSendInstantMessage jnErrorUnableToSendInstantMessage = new JnErrorUnableToSendInstantMessage(json);
				throw jnErrorUnableToSendInstantMessage;
			}
			
			Integer sleepToSendMessage = this.getSleepTimeToRetry();
			CcpTimeDecorator timer = new CcpTimeDecorator();

			timer.sleep(sleepToSendMessage);
			int nextTry = triesToSendMessage + 1;
			CcpJsonRepresentation jsonWithNextTry = json.put(InstantMessengerApiFields.triesToSendMessage, nextTry);
			CcpJsonRepresentation retryResponse = this.execute(jsonWithNextTry);
			return retryResponse;
		}

		private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation json, String botName) {
			CcpJsonRepresentation blockedBotRecord = json.put(JnJsonInstantMessengerFields.botName, botName);
			JnEntityInstantMessengerBotLocked.ENTITY.save(blockedBotRecord);
			return json;
		}

		public CcpJsonRepresentation getParameters(CcpJsonRepresentation json) {
			
			String botName = json.getOrDefault(JnJsonInstantMessengerFields.botName, () -> JnBotType.support.name());
			
			JnBotType botType = JnBotType.valueOf(botName);
			
			CcpJsonRepresentation parameters = botType.getParameters(json);
			
			return parameters;
		}

	}
	;


	
	public static enum InstantMessengerApiFields implements CcpJsonFieldName{
		triesToSendMessage, 
		bots
	}
	
	public static enum JnBotType implements CcpJsonFieldName{
		support {
			CcpJsonRepresentation getParameters(CcpJsonRepresentation json) {
				String supportLanguage =  JnSystemProperties.INSTANCE.supportLanguage();

			return json
						.put(JnJsonInstantMessengerFields.botName, this.name())
						.put(JnJsonCommonsFields.language, supportLanguage);
			}
		},
		user {
			CcpJsonRepresentation getParameters(CcpJsonRepresentation json) {
				return json
						.put(JnJsonInstantMessengerFields.botName, this.name())
						;
			}
		},
		;
		abstract CcpJsonRepresentation getParameters(CcpJsonRepresentation json);
	
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
	public abstract CcpJsonRepresentation getParameters(CcpJsonRepresentation json);
	public abstract JnAddDefaultStep addDefaultProcessToSendMessage(JnSendMessageToUser sender, JnMessageSenderExceptionHandler exceptionHandler);
}
