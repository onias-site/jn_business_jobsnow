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

/**
 * Kinds of instant message: a text or a file. Each one has its own input rules and sends through
 * {@code CcpInstantMessenger}; the {@code message}, {@code caption} and {@code fileName} are templates resolved with the
 * values of the JSON.
 */
public enum JnInstantMessageType implements CcpBusiness{
	/** A text message, replying to {@code replyTo} when given. */
	text(JnMessageTextJsonValidator.class) {
		/**
		 * Sends the text.
		 * @param json the values of the message, with the bot token and the chat id
		 * @param messageFields the templates of the message
		 * @return the JSON merged with the answer of the provider
		 */
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			String message = super.getMessage(json, messageFields, JnJsonCommonsFields.message);
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken);
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = super.getReplyTo(json);
			CcpStringDecorator botNameDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);
			CcpJsonFieldName botName = botNameDecorator.jsonFieldName();
			CcpJsonRepresentation result = instantMessenger.sendTextMessage(botName, botToken, chatId, replyTo, message);
			CcpJsonRepresentation jsonWithResult = json.mergeWithAnotherJson(result);
			return jsonWithResult;
		}

	},
	/** A file whose content is the message, with a caption and a file name. */
	file(JnMessageFileJsonValidator.class) {
		/**
		 * Sends the file.
		 * @param json the values of the message, with the bot token and the chat id
		 * @param messageFields the templates of the message, the caption and the file name
		 * @return the JSON merged with the answer of the provider
		 */
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken) ;
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = super.getReplyTo(json);
			
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
	/** The input rules of the kind. */
	private final Class<?> jsonValidationClass;
	
	/**
	 * Returns the input rules of the kind.
	 * @return the validation class
	 */
	public Class<?> getJsonValidationClass() {
		return this.jsonValidationClass;
	}
	
	/**
	 * Associates the kind with its input rules.
	 * @param jsonValidationClass the input rules
	 */
	private JnInstantMessageType(Class<?> jsonValidationClass) {
		this.jsonValidationClass = jsonValidationClass;
	}
	/**
	 * Returns the message this one answers, or 0 when it answers none. Read as a number, never cast: after going through
	 * JSON (Gson, the database, Pub/Sub) the value comes back as a double. Until 2026-10-06 the file sending cast it to
	 * {@code Long} and failed with {@code ClassCastException}.
	 * @param json the values of the message
	 * @return the id of the message answered, or 0
	 */
	protected Long getReplyTo(CcpJsonRepresentation json) {
		boolean answersNoMessage = false == json.containsAllFields(CcpJsonCommonsFields.replyTo);

		if(answersNoMessage) {
			return 0L;
		}

		Long replyTo = json.getAsLongNumber(CcpJsonCommonsFields.replyTo);
		return replyTo;
	}

	/**
	 * Resolves the template of a field with the values of the JSON.
	 * @param json the values
	 * @param messageFields the templates
	 * @param field the field of the template
	 * @return the resolved text
	 */
	protected String getMessage(CcpJsonRepresentation json, CcpJsonRepresentation messageFields, CcpJsonFieldName field) {
		CcpTextDecorator template = messageFields.getAsTextDecorator(field);
		CcpTextDecorator resolvedMessage = template.resolveTemplate(json);
		return resolvedMessage.content;
	}
	/**
	 * Sends the message.
	 * @param json the values of the message
	 * @return the JSON merged with the answer of the provider
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation messageFields = json.getJsonPiece(JnJsonInstantMessengerFields.fileName, JnJsonInstantMessengerFields.caption, JnJsonCommonsFields.message, CcpJsonCommonsFields.replyTo, JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation sentMessage = this.sendMessage(json, messageFields);
		return sentMessage;
	}

	/**
	 * Sends the message of this kind.
	 * @param json the values of the message
	 * @param messageFields the templates
	 * @return the JSON merged with the answer of the provider
	 */
	public abstract CcpJsonRepresentation sendMessage (CcpJsonRepresentation json, CcpJsonRepresentation messageFields);

	
	/** Input rules of a text message. */
	private static enum JnMessageTextJsonValidator implements CcpJsonFieldName{
		/** The {@code message} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message,
		/** The {@code botToken} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botToken,
		;
	}

	/** Input rules of a file message. */
	private static enum JnMessageFileJsonValidator implements CcpJsonFieldName{
		/** The {@code caption} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		/** The {@code contentType} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType,
		/** The {@code message} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message,
		/** The {@code botToken} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botToken,
		/** The {@code fileName} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName

		;
	}

}
