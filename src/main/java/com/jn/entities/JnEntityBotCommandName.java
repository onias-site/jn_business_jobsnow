package com.jn.entities;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionablePurge;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * The name of a bot command in a language: how the user types it. In English the commands keep the canonical name,
 * which needs no record. Each cost center seeds the names of its own commands.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_bot_command_name}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JnEntityBotCommandName.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityBotCommandName implements CcpEntityConfigurator {

	/** The entity {@code jn_bot_command_name}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityBotCommandName.class).entityInstance;
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code commandName} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		commandName, 
		/** The {@code language} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language,
		/** The {@code message} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message, 
		;
	}
	
	/**
	 * The seed records of the Portuguese names of some commands: the name of each item is the canonical name of the
	 * command and {@code getValue()} its name in Portuguese. Each cost center calls it with the enum of its own commands.
	 * @param commandNamesInPortuguese the commands
	 * @return the seed records
	 */
	public static List<CcpBulkItem> getPortugueseNames(CcpJsonFieldName... commandNamesInPortuguese) {
		String portugueseName = JnLanguage.portuguese.name();
		List<CcpBulkItem> records = new ArrayList<>();

		for (CcpJsonFieldName commandName : commandNamesInPortuguese) {
			String canonicalName = commandName.name();
			String nameInPortuguese = commandName.getValue();
			CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
					.put(JnJsonInstantMessengerFields.commandName, canonicalName)
					.put(JnJsonInstantMessengerFields.message, nameInPortuguese)
					.put(JnJsonCommonsFields.language, portugueseName);
			List<CcpBulkItem> bulkItems = ENTITY.toBulkItems(record, CcpBulkEntityOperationType.create);
			records.addAll(bulkItems);
		}

		return records;
	}

}
