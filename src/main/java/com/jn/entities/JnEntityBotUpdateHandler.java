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
 * The business that handles an update pushed by the instant messenger to a bot, by class name: the cost center of the
 * bot engine (jb) seeds it, and the API that receives the webhook (js) runs it without importing that cost center, the
 * same way the APIs run the validator of the session.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_bot_update_handler}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JnEntityBotUpdateHandler.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityBotUpdateHandler implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityBotUpdateHandler INSTANCE = new JnEntityBotUpdateHandler();

	private JnEntityBotUpdateHandler() {}

	/** The entity {@code jn_bot_update_handler}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityBotUpdateHandler.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code botName} field: the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName,
		/** The {@code business} field: the name of the class of the business that handles the updates of the bot. */
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
	 * @param business the class of the business that handles the updates of the bot
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
