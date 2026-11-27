package com.jn.entities;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * The business that handles a message written in a group (or supergroup) of the instant messenger where a bot is, by
 * class name (since 2026-11-27): the bots run no command out of the private chat, so the message of a group goes to this
 * business instead of the bot engine. The cost center of the data the business works on seeds it (vis seeds the job
 * postings of the groups for the support bot, {@code vis_instant_message_position_reply}), and the bot engine (jb) runs
 * it in the queue {@code JbBusinessHandleGroupMessage~<bot>} without importing that cost center. A bot without a record
 * ignores the messages of its groups.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_bot_group_message_handler}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JnEntityBotGroupMessageHandler.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityBotGroupMessageHandler implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityBotGroupMessageHandler INSTANCE = new JnEntityBotGroupMessageHandler();

	private JnEntityBotGroupMessageHandler() {}

	/** The entity {@code jn_bot_group_message_handler}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityBotGroupMessageHandler.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code botName} field: the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName,
		/** The {@code business} field: the name of the class of the business that handles the messages of the groups of the bot. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 200)
		business,
		;
	}

	/**
	 * The key of the handler of the given bot.
	 * @param botName the name of the bot
	 * @return the key
	 */
	public static CcpJsonRepresentation getKey(String botName) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(Fields.botName, botName);
		return key;
	}

	/**
	 * The seed of the handler of the given bot, for the {@code getFirstRecordsToInsert} of an entity of the cost center
	 * that owns the business.
	 * @param botName the name of the bot
	 * @param business the class of the business that handles the messages of the groups of the bot
	 * @return the seed records
	 */
	public static List<CcpBulkItem> getSeeds(String botName, Class<? extends CcpBusiness> business) {
		CcpJsonRepresentation key = getKey(botName);
		String businessName = business.getName();
		CcpJsonRepresentation handler = key.put(Fields.business, businessName);
		List<CcpBulkItem> seeds = ENTITY.toBulkItems(handler, CcpBulkEntityOperationType.create);
		return seeds;
	}
}
