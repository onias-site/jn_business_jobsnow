package com.jn.business.commons;


import com.ccp.decorators.CcpJsonRepresentation;
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

public class JnBusinessSendInstantMessage {
	enum JsonFieldNames implements CcpJsonFieldName{
		maxTriesToSendMessage, triesToSendMessage, sleepToSendMessage
	}

	public static final JnBusinessSendInstantMessage INSTANCE = new JnBusinessSendInstantMessage();
	
	private JnBusinessSendInstantMessage() {
		
	}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		String token = instantMessenger.getToken(json);
		
		long totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia = new CcpTimeDecorator().getSecondsEnlapsedSinceMidnight();
		json = json.put(JnEntityInstantMessengerMessageSent.Fields.interval, totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia / 3).put(JnEntityInstantMessengerMessageSent.Fields.token, token);
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
			CcpJsonRepresentation instantMessengerData = instantMessenger.sendMessage(json);
			CcpJsonRepresentation instantMessageSent = json.mergeWithAnotherJson(instantMessengerData);
			JnEntityInstantMessengerMessageSent.ENTITY.save(instantMessageSent);
			return json;
		} catch (CcpErrorInstantMessageTooManyRequests e) {
			
			return this.retryToSendMessage(json);
			
		} catch(CcpErrorInstantMessageThisBotWasBlockedByThisUser e) {
			return saveBlockedBot(json, e.token);
		}
	}

	private CcpJsonRepresentation retryToSendMessage(CcpJsonRepresentation json) {
		
		Integer maxTriesToSendMessage = json.getAsIntegerNumber(JsonFieldNames.maxTriesToSendMessage);
		Integer triesToSendMessage = json.getOrDefault(JsonFieldNames.triesToSendMessage, 1);
		
		if(triesToSendMessage >= maxTriesToSendMessage) {
			throw new JnErrorUnableToSendInstantMessage(json);
		}
		
		Integer sleepToSendMessage = json.getAsIntegerNumber(JsonFieldNames.sleepToSendMessage);
		
		new CcpTimeDecorator().sleep(sleepToSendMessage);
		CcpJsonRepresentation put = json.put(JsonFieldNames.triesToSendMessage, triesToSendMessage + 1);
		CcpJsonRepresentation apply = this.apply(put);
		return apply;
	}

	private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
		JnEntityInstantMessengerBotLocked.ENTITY.save(putAll.put(JnEntityInstantMessengerBotLocked.Fields.token, token));
		return putAll;
	}

}
