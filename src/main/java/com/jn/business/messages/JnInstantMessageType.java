package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

public enum JnInstantMessageType implements CcpBusiness{
	text(JnBusinessSendInstantMessage.JnMessageTextJsonValidator.class) {
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			String message = super.getMessage(json, orElseThrow, JnJsonCommonsFields.message);
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken);
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = Double.valueOf(json.getOrDefault(JnBusinessSendInstantMessage.Fields.replyTo, () -> (Object)"0").toString()).longValue();
			CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);
			CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
			CcpJsonRepresentation result = instantMessenger.sendTextMessage(jsonFieldName, botToken, chatId, replyTo, message);
			CcpJsonRepresentation mergeWithAnotherJson = json.mergeWithAnotherJson(result);
			return mergeWithAnotherJson;
		}

	},
	file(JnBusinessSendInstantMessage.JnMessageFileJsonValidator.class) {
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
			CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
			
			String botToken = json.getAsString(JnJsonInstantMessengerFields.botToken) ;
			Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			Long replyTo = json.getOrDefault(JnBusinessSendInstantMessage.Fields.replyTo, () -> 0L);
			
			String message = super.getMessage(json, orElseThrow, JnJsonCommonsFields.message);
			String caption = super.getMessage(json, orElseThrow, JnJsonInstantMessengerFields.caption);
			String fileName = super.getMessage(json, orElseThrow, JnJsonInstantMessengerFields.fileName);
			CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(message);

			Byte[] bytes = ccpStringDecorator.getBytes();
			CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonInstantMessengerFields.botName);
			CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
			CcpJsonRepresentation result = instantMessenger.sendFile(jsonFieldName, botToken, chatId, replyTo, fileName, caption, bytes);
			CcpJsonRepresentation mergeWithAnotherJson = json.mergeWithAnotherJson(result);
			return mergeWithAnotherJson;
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
		CcpJsonRepresentation message = json.getJsonPiece(JnJsonInstantMessengerFields.fileName, JnJsonInstantMessengerFields.caption, JnJsonCommonsFields.message, JnBusinessSendInstantMessage.Fields.replyTo, JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation sendMessage = this.sendMessage(json, message);
		return sendMessage;
	}

	public abstract CcpJsonRepresentation sendMessage (CcpJsonRepresentation json, CcpJsonRepresentation message);

}
