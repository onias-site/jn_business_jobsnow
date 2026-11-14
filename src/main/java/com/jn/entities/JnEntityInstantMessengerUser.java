package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberInteger;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The chat of the instant messenger of each user (since 2026-11-13): one record per e-mail, the one informed in the
 * command of the bot, kept as its SHA-1 hash, with the chat that answers for it.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_instant_messenger_user}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityInstantMessengerUser.Fields.class)
public class JnEntityInstantMessengerUser implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityInstantMessengerUser INSTANCE = new JnEntityInstantMessengerUser();

	private JnEntityInstantMessengerUser() {}

	/** The entity {@code jn_instant_messenger_user}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerUser.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: the primary key, the e-mail informed in the command, stored as its SHA-1 hash. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,
		/** The {@code chatId} field: required, an integer (long), the chat of the user in the instant messenger. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberInteger
		chatId
		;
	}
}
