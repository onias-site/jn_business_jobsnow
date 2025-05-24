package com.jn.business.commons;


import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.exceptions.instant.messenger.CcpInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.exceptions.instant.messenger.CcpTooManyRequests;
import com.jn.entities.JnEntityHttpApiParameters;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.exceptions.JnUnableToSendInstantMessage;
import com.jn.utils.JnDeleteKeysFromCache;


public class JnBusinessSendInstantMessage {

	
	public static final JnBusinessSendInstantMessage INSTANCE = new JnBusinessSendInstantMessage();
	
	private JnBusinessSendInstantMessage() {
		
	}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpInstantMessenger instantMessenger = CcpDependencyInjection.getDependency(CcpInstantMessenger.class);
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		String token = instantMessenger.getToken(json);
		
		long totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia = new CcpTimeDecorator().getSecondsEnlapsedSinceMidnight();
		json = json.put(JnEntityInstantMessengerMessageSent.Fields.interval.name(), totalDeSegundosDecorridosDesdeMeiaNoiteDesteDia / 3).put(JnEntityInstantMessengerMessageSent.Fields.token.name(), token);
		CcpSelectUnionAll dataFromThisRecipient = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, JnEntityInstantMessengerBotLocked.ENTITY, JnEntityInstantMessengerMessageSent.ENTITY);

		boolean thisRecipientRecentlyReceivedThisMessageFromThisBot =  JnEntityInstantMessengerMessageSent.ENTITY.isPresentInThisUnionAll(dataFromThisRecipient , json);

		if(thisRecipientRecentlyReceivedThisMessageFromThisBot) {
			Integer sleep = json.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.sleep.name());
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
			CcpJsonRepresentation instantMessageSent = json.putAll(instantMessengerData);
			JnEntityInstantMessengerMessageSent.ENTITY.createOrUpdate(instantMessageSent);
			return json;
		} catch (CcpTooManyRequests e) {
			
			return this.retryToSendMessage(json);
			
		} catch(CcpInstantMessageThisBotWasBlockedByThisUser e) {
			return saveBlockedBot(json, e.token);
		}
	}

	private CcpJsonRepresentation retryToSendMessage(CcpJsonRepresentation json) {
		
		Integer maxTriesToSendMessage = json.getAsIntegerNumber("maxTriesToSendMessage");
		Integer triesToSendMessage = json.getOrDefault("triesToSendMessage", 1);
		
		if(triesToSendMessage >= maxTriesToSendMessage) {
			throw new JnUnableToSendInstantMessage(json);
		}
		
		Integer sleepToSendMessage = json.getAsIntegerNumber("sleepToSendMessage");
		
		new CcpTimeDecorator().sleep(sleepToSendMessage);
		CcpJsonRepresentation put = json.put("triesToSendMessage", triesToSendMessage + 1);
		CcpJsonRepresentation apply = this.apply(put);
		return apply;
	}

	private CcpJsonRepresentation saveBlockedBot(CcpJsonRepresentation putAll, String token) {
		JnEntityInstantMessengerBotLocked.ENTITY.createOrUpdate(putAll.put(JnEntityInstantMessengerBotLocked.Fields.token.name(), token));
		return putAll;
	}

}
