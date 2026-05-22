package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.exceptions.JnErrorUnableToSendInstantMessage;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnSystemProperties;

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
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		templateId,
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
	private final JnSystemProperties systemProperties = new JnSystemProperties();
	
	private JnBusinessSendInstantMessage() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpJsonFieldName botName = json.getAsStringDecorator(JnJsonValidator.botName).jsonFieldName();
		
		String botToken = this.systemProperties.getSystemInnerProperty(Fields.bots, botName);
		
		CcpJsonRepresentation jsonWithBotToken = json.put(JnMessageFileJsonValidator.botToken, botToken);
		String messageType = jsonWithBotToken.getAsString(JnJsonValidator.instantMessageType);
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
		
		if(triesToSendMessage >= maxTriesToSendMessage) {
			throw new JnErrorUnableToSendInstantMessage(json);
		}
		
		Integer sleepToSendMessage = this.getSleepTimeToRetry();
		
		new CcpTimeDecorator().sleep(sleepToSendMessage);
		CcpJsonRepresentation put = json.put(Fields.triesToSendMessage, triesToSendMessage + 1);
		CcpJsonRepresentation apply = this.apply(put);
		return apply;
	}

	private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
		JnEntityInstantMessengerBotLocked.ENTITY.save(putAll.put(JnEntityInstantMessengerBotLocked.Fields.botName, token));
		return putAll;
	}
	public static enum JnInstantMessageType implements CcpBusiness{
		text(JnMessageTextJsonValidator.class) {
			public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
				CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
				String message = super.getMessage(json, orElseThrow, JnMessageTextJsonValidator.message);
				String botToken = json.getAsString(JnMessageTextJsonValidator.botToken) ;
				Long chatId = json.getAsLongNumber(JnJsonValidator.chatId);
				Long replyTo = json.getOrDefault(Fields.replyTo, () -> 0d).longValue();
				CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonValidator.botName);
				CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
				CcpJsonRepresentation result = instantMessenger.sendTextMessage(jsonFieldName, botToken, chatId, replyTo, message);
				CcpJsonRepresentation jsonPiece = result.getJsonPiece(JnMessageTextJsonValidator.values());
				return jsonPiece;
			}


		},
		file(JnMessageFileJsonValidator.class) {
			public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
				CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
				
				String botToken = json.getAsString(JnMessageTextJsonValidator.botToken) ;
				Long chatId = json.getAsLongNumber(JnJsonValidator.chatId);
				Long replyTo = json.getOrDefault(Fields.replyTo, () -> 0d).longValue();
				
				String message = super.getMessage(json, orElseThrow, JnMessageTextJsonValidator.message);
				String caption = super.getMessage(json, orElseThrow, JnMessageFileJsonValidator.caption);
				String fileName = super.getMessage(json, orElseThrow, JnMessageFileJsonValidator.fileName);

				Byte[] bytes = new CcpStringDecorator(message).getBytes();
				CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonValidator.botName);
				CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
				CcpJsonRepresentation result = instantMessenger.sendFile(jsonFieldName, botToken, chatId, replyTo, fileName, caption, bytes);
				CcpJsonRepresentation jsonPiece = result.getJsonPiece(JnMessageFileJsonValidator.values());
				return jsonPiece;
			}
		}
		;
		private final Class<?> jsonValidationClass;
		
		public Class<?> getJsonValidationClass() {
			return this.jsonValidationClass;
		}
		
		private JnInstantMessageType(Class<?> jsonValidationClass) {
			this.jsonValidationClass = jsonValidationClass;
		}
		protected String getMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow, CcpJsonFieldName field) {
			CcpTextDecorator text = orElseThrow.getAsTextDecorator(field);
			CcpTextDecorator message = text.resolveTemplate(json);
			return message.content;
		}
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonRepresentation message = json.getJsonPiece(JnMessageFileJsonValidator.fileName, JnMessageFileJsonValidator.caption, JnMessageTextJsonValidator.message, Fields.replyTo, JnJsonValidator.chatId);
			CcpJsonRepresentation sendMessage = this.sendMessage(json, message);
			return sendMessage;
		}
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, String message) {
			CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(JnMessageTextJsonValidator.message, message);
			CcpJsonRepresentation sendMessage = this.sendMessage(json, put);
			return sendMessage;
		}
		
		
		public abstract CcpJsonRepresentation sendMessage (CcpJsonRepresentation json, CcpJsonRepresentation message);
		
	}

}
