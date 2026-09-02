package com.jn.business.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

import com.jn.utils.JnSystemProperties;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Envia mensagens instantâneas via Telegram usando bots configurados. Suporta dois
 * tipos de mensagem (text e file) definidos no enum interno JnInstantMessageType.
 * Implementa retentativa em caso de CcpHttpTooManyRequests e trata o bloqueio do
 * bot pelo usuário salvando em JnEntityInstantMessengerBotLocked.
 */
public class JnBusinessSendInstantMessage implements CcpHttpApiExecutor{
	public static enum Fields implements CcpJsonFieldName{
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
	
	public static enum JnMessageTextJsonValidator implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botToken,
		;
	}

	public static enum JnJsonValidator implements CcpJsonFieldName{
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
	
	public static enum JnMessageFileJsonValidator implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		contentType,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botToken,
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName

		;
	}

	public Class<?> getJsonValidationClass() {
		return JnJsonValidator.class;
	}
	
	public static final JnBusinessSendInstantMessage INSTANCE = new JnBusinessSendInstantMessage();
	
	private JnBusinessSendInstantMessage() {}
	
	/**
	 * Obtém o token do bot via JnSystemProperties, determina o tipo de mensagem, tenta
	 * enviar e trata exceções de rate-limit e bloqueio de bot.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);

		CcpJsonFieldName botName = asStringDecorator.jsonFieldName();
		
		String botToken =  JnSystemProperties.INSTANCE.getSystemInnerProperty(Fields.bots, botName);
		
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
		Integer triesToSendMessage = json.getOrDefault(Fields.triesToSendMessage, () -> 1);
		boolean triesToSendMessageMaiorOuIgual = triesToSendMessage >= maxTriesToSendMessage;

		if(triesToSendMessageMaiorOuIgual) {
			JnErrorUnableToSendInstantMessage jnErrorUnableToSendInstantMessage = new JnErrorUnableToSendInstantMessage(json);
			throw jnErrorUnableToSendInstantMessage;
		}
		
		Integer sleepToSendMessage = this.getSleepTimeToRetry();
		CcpTimeDecorator ccpTimeDecorator = new CcpTimeDecorator();

		ccpTimeDecorator.sleep(sleepToSendMessage);
		int triesToSendMessageMais = triesToSendMessage + 1;
		CcpJsonRepresentation put = json.put(Fields.triesToSendMessage, triesToSendMessageMais);
		CcpJsonRepresentation apply = this.execute(put);
		return apply;
	}

	private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
		CcpJsonRepresentation put2 = putAll.put(JnJsonInstantMessengerFields.botName, token);
		JnEntityInstantMessengerBotLocked.ENTITY.save(put2);
		return putAll;
	}


	@SuppressWarnings("serial")
	public static class JnErrorUnableToSendInstantMessage extends RuntimeException {
		private JnErrorUnableToSendInstantMessage(CcpJsonRepresentation json) {
			super("This message couldn't be sent. Details: " + json);
		}
	}

}
