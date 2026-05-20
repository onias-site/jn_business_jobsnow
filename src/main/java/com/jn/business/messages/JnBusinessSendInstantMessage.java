package com.jn.business.messages;


import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.jn.entities.JnEntityHttpApiParameters;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.exceptions.JnErrorUnableToSendInstantMessage;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnSystemProperties;

public class JnBusinessSendInstantMessage implements CcpBusiness{
	enum Fields implements CcpJsonFieldName{
		maxTriesToSendMessage, triesToSendMessage, sleepToSendMessage, message, bots, botName, chatId, replyTo, caption, fileName
	}

	public static final JnBusinessSendInstantMessage INSTANCE = new JnBusinessSendInstantMessage();
	
	private JnBusinessSendInstantMessage() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		
		long totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia = new CcpTimeDecorator().getSecondsEnlapsedSinceMidnight();
		json = json.put(JnEntityInstantMessengerMessageSent.Fields.interval, totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia / 3);
		CcpSelectUnionAll dataFromThisRecipient = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, JnEntityInstantMessengerBotLocked.ENTITY, JnEntityInstantMessengerMessageSent.ENTITY);

		boolean thisRecipientRecentlyReceivedThisMessageFromThisBot =  JnEntityInstantMessengerMessageSent.ENTITY.isPresentInThisUnionAll(dataFromThisRecipient , json);

		if(thisRecipientRecentlyReceivedThisMessageFromThisBot) {
			Integer sleep = json.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.sleep);
			new CcpTimeDecorator().sleep(sleep);
			CcpJsonRepresentation execute = this.apply(json);
			return execute;
		}

		boolean thisBotHasBeenBlocked = JnEntityInstantMessengerBotLocked.ENTITY.isPresentInThisUnionAll(dataFromThisRecipient, json);
		
		if(thisBotHasBeenBlocked) {
			return json;
		}
		
		try {
			String botName = json.getAsString(Fields.botName);
			MessageType instantMessenger = MessageType.valueOf(botName);
			CcpJsonRepresentation instantMessengerData = instantMessenger.sendMessage(json);
			CcpJsonRepresentation instantMessageSent = json.mergeWithAnotherJson(instantMessengerData);
			JnEntityInstantMessengerMessageSent.ENTITY.save(instantMessageSent);
			return json;
		} catch (CcpErrorInstantMessageTooManyRequests e) {
			return this.retryToSendMessage(json);
			
		} catch(CcpErrorInstantMessageThisBotWasBlockedByThisUser e) {
			return saveBlockedBot(json, e.botName);
		}
	}

	private CcpJsonRepresentation retryToSendMessage(CcpJsonRepresentation json) {
		
		Integer maxTriesToSendMessage = json.getAsIntegerNumber(Fields.maxTriesToSendMessage);
		Integer triesToSendMessage = json.getOrDefault(Fields.triesToSendMessage, () -> 1);
		
		if(triesToSendMessage >= maxTriesToSendMessage) {
			throw new JnErrorUnableToSendInstantMessage(json);
		}
		
		Integer sleepToSendMessage = json.getAsIntegerNumber(Fields.sleepToSendMessage);
		
		new CcpTimeDecorator().sleep(sleepToSendMessage);
		CcpJsonRepresentation put = json.put(Fields.triesToSendMessage, triesToSendMessage + 1);
		CcpJsonRepresentation apply = this.apply(put);
		return apply;
	}

	private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
		JnEntityInstantMessengerBotLocked.ENTITY.save(putAll.put(JnEntityInstantMessengerBotLocked.Fields.botName, token));
		return putAll;
	}
	public static enum MessageType{
		text {
			public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
				String message = super.getMessage(json, orElseThrow, Fields.message);
				String botToken = this.systemProperties.getSystemInnerProperty(Fields.bots, Fields.botName) ;
				Long chatId = json.getAsLongNumber(Fields.chatId);
				Long replyTo = json.getAsLongNumber(Fields.replyTo);
				CcpStringDecorator asStringDecorator = json.getAsStringDecorator(Fields.botName);
				CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
				CcpJsonRepresentation sendTextMessage = this.instantMessenger.sendTextMessage(jsonFieldName, botToken, chatId, replyTo, message);
				return sendTextMessage;
			}

		},
		file {
			public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow) {
				
				String botToken = this.systemProperties.getSystemInnerProperty(Fields.bots, Fields.botName) ;
				Long chatId = json.getAsLongNumber(Fields.chatId);
				Long replyTo = json.getAsLongNumber(Fields.replyTo);
				
				String message = super.getMessage(json, orElseThrow, Fields.message);
				String caption = super.getMessage(json, orElseThrow, Fields.caption);
				String fileName = super.getMessage(json, orElseThrow, Fields.fileName);

				Byte[] bytes = new CcpStringDecorator(message).getBytes();
				CcpStringDecorator asStringDecorator = json.getAsStringDecorator(Fields.botName);
				CcpJsonFieldName jsonFieldName = asStringDecorator.jsonFieldName();
				CcpJsonRepresentation sendTextMessage = this.instantMessenger.sendFile(jsonFieldName, botToken, chatId, replyTo, fileName, caption, bytes);
				return sendTextMessage;
			}
		}
		;
		protected String getMessage(CcpJsonRepresentation json, CcpJsonRepresentation orElseThrow, CcpJsonFieldName field) {
			CcpTextDecorator text = orElseThrow.getAsTextDecorator(field);
			CcpTextDecorator message = text.resolveTemplate(json);
			return message.content;
		}
		protected CcpJsonRepresentation sendMessage(CcpJsonRepresentation json) {
			CcpJsonRepresentation message = json.getJsonPiece(Fields.fileName, Fields.caption, Fields.message, Fields.replyTo, Fields.chatId);
			CcpJsonRepresentation sendMessage = this.sendMessage(json, message);
			return sendMessage;
		}
		public CcpJsonRepresentation sendMessage(CcpJsonRepresentation json, String message) {
			CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(Fields.message, message);
			CcpJsonRepresentation sendMessage = this.sendMessage(json, put);
			return sendMessage;
		}
		
		
		public abstract CcpJsonRepresentation sendMessage (CcpJsonRepresentation json, CcpJsonRepresentation message);
		
		CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
		JnSystemProperties systemProperties = new JnSystemProperties();
	}

}
