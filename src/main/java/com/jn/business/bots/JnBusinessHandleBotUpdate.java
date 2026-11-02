package com.jn.business.bots;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.jn.entities.JnEntityBotUpdateHandler;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Hands an update pushed by the instant messenger to the business seeded for its bot in
 * {@link JnEntityBotUpdateHandler}, run by its class name: the API that receives the webhook reaches the bot engine
 * without importing its cost center.
 */
public class JnBusinessHandleBotUpdate implements CcpBusiness {

	/** The single instance. */
	public static final JnBusinessHandleBotUpdate INSTANCE = new JnBusinessHandleBotUpdate();

	private JnBusinessHandleBotUpdate() {}

	/**
	 * Runs the seeded handler over the update.
	 * @param json the update, with {@code botName}
	 * @return what the seeded handler answers
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String botName = json.getAsString(JnJsonInstantMessengerFields.botName);
		CcpJsonRepresentation key = JnEntityBotUpdateHandler.getKey(botName);
		CcpJsonRepresentation handler = JnEntityBotUpdateHandler.ENTITY.getOneById(key);
		String businessName = handler.getAsString(JnEntityBotUpdateHandler.Fields.business);
		CcpStringDecorator businessNameDecorator = new CcpStringDecorator(businessName);
		CcpReflectionConstructorDecorator reflection = businessNameDecorator.reflection();
		CcpBusiness business = reflection.newInstance();
		CcpJsonRepresentation handled = business.execute(json);
		return handled;
	}
}
