package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.decorators.annotations.JnEntityDisposable;
import com.jn.entities.decorators.builders.JnEntityDisposableBuilder;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldEntityMessageHash;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Instant messages sent successfully; the {@code message} is part of the key (as a SHA-1), so the same message is not sent twice to the same chat while the record lives.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_instant_messenger_message_sent}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>disposable: records expire by the {@code hourly} granularity</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityDisposableBuilder.class, priority = 1),})
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.hourly)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerMessageSent.Fields.class)
public class JnEntityInstantMessengerMessageSent implements CcpEntityConfigurator {
	
	/** The entity {@code jn_instant_messenger_message_sent}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerMessageSent.class).entityInstance;

	
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
		/** The {@code instantMessageType} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType,
		/** The {@code caption} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,
		/** The {@code contentType} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType,
		/** The {@code fileName} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName,
		/** The {@code message} field: part of the primary key, transformed by {@code JnJsonTransformersFieldEntityMessageHash}. */
		@CcpEntityFieldPrimaryKey
		@CcpEntityFieldTransformer(JnJsonTransformersFieldEntityMessageHash.class)
		message,

		;
	}

}
