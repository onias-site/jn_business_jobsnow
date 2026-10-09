package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Commands sent to the support operator that stopped making sense before the operator ran them, because the user gave
 * up what the command was about (a skill suggestion or a skill hierarchy fix request withdrawn). Written by
 * {@code JnBusinessCancelSupportPendingCommand}, with the same key as {@link JnEntitySupportPendingCommand}. The
 * support bot (jb, which the cost centers that send the commands can not see) reads these records when it lists the
 * tickets of the operator, deletes the tickets they name and then deletes them. A command sent again after being
 * cancelled deletes its cancellation ({@code JnBusinessRegisterSupportPendingCommand}), so the new ticket survives. No
 * cache: the record is written by one process and read by another.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_support_cancelled_command}</li>
 * </ul>
 */
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySupportCancelledCommand.Fields.class)
public class JnEntitySupportCancelledCommand implements CcpEntityConfigurator {

	/** The entity {@code jn_support_cancelled_command}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySupportCancelledCommand.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code botName} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		botName,
		/** The {@code chatId} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId,
		/** The {@code command} field: part of the primary key, text, normalized as in {@link JnEntitySupportPendingCommand}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		command,
		/** The {@code timestamp} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp,
		;
	}
}
