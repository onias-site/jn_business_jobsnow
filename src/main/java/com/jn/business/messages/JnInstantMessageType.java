package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

public enum JnInstantMessageType implements CcpBusiness{
	text(JnMessageTextJsonValidator.class) {
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			String message = super.getMessage(json, messageFields, JnJsonCommonsFields.message);
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken);
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = Double.valueOf(json.getOrDefault(CcpJsonCommonsFields.replyTo, () -> (Object)"0").toString()).longValue();
			CcpStringDecorator botNameDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);
			CcpJsonFieldName botName = botNameDecorator.jsonFieldName();
			CcpJsonRepresentation result = instantMessenger.sendTextMessage(botName, botToken, chatId, replyTo, message);
			CcpJsonRepresentation jsonWithResult = json.mergeWithAnotherJson(result);
			return jsonWithResult;
		}

	},
	file(JnMessageFileJsonValidator.class) {
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken) ;
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = json.getOrDefault(CcpJsonCommonsFields.replyTo, () -> 0L);
			
			String message = super.getMessage(json, messageFields, JnJsonCommonsFields.message);
			String caption = super.getMessage(json, messageFields, JnJsonInstantMessengerFields.caption);
			String fileName = super.getMessage(json, messageFields, JnJsonInstantMessengerFields.fileName);
			CcpStringDecorator messageDecorator = new CcpStringDecorator(message);

			Byte[] bytes = messageDecorator.getBytes();
			CcpStringDecorator botNameDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);
			CcpJsonFieldName botName = botNameDecorator.jsonFieldName();
			CcpJsonRepresentation result = instantMessenger.sendFile(botName, botToken, chatId, replyTo, fileName, caption, bytes);
			CcpJsonRepresentation jsonWithResult = json.mergeWithAnotherJson(result);
			return jsonWithResult;
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
	protected String getMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields, CcpJsonFieldName field) {
		CcpTextDecorator template = messageFields.getAsTextDecorator(field);
		CcpTextDecorator resolvedMessage = template.resolveTemplate(json);
		return resolvedMessage.content;
	}
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation messageFields = json.getJsonPiece(JnJsonInstantMessengerFields.fileName, JnJsonInstantMessengerFields.caption, JnJsonCommonsFields.message, CcpJsonCommonsFields.replyTo, JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation sentMessage = this.sendMessage(json, messageFields);
		return sentMessage;
	}

	public abstract CcpJsonRepresentation sendMessage (CcpJsonRepresentation json, CcpJsonRepresentation messageFields);

	
	private static enum JnMessageTextJsonValidator implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botToken,
		;
	}

	private static enum JnMessageFileJsonValidator implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
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

}
