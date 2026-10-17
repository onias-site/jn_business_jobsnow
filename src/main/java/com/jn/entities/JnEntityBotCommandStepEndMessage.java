package com.jn.entities;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * A message sent after the engine of a step runs, by language; its text may refer to the values the engine produced.
 * Each cost center seeds the end messages of the steps of its own commands.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_bot_command_step_end_message}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityBotCommandStepEndMessage.Fields.class)
public class JnEntityBotCommandStepEndMessage implements CcpEntityConfigurator {

	/** The entity {@code jn_bot_command_step_end_message}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityBotCommandStepEndMessage.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code stepName} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		stepName,
		/** The {@code language} field: part of the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		language,
		/** The {@code message} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		message,

		/** The {@code instantMessageType} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		instantMessageType,

		//ATTENTION PODE SER DE IDIOMA DIFERENTE
		/** The {@code caption} field: text, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldTypeString
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		caption,

		/** The {@code contentType} field: text, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldTypeString
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType,

		/** The {@code fileName} field: text, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldTypeString
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		fileName

		;
	}

	/**
	 * The end messages of steps that end by sending the operator the text their engine left in {@code botReply} (the
	 * request, the next item, the next ticket, and so on), already written in the language of the session: one record
	 * per step and language.
	 * @param stepNames the steps
	 * @return the records
	 */
	public static List<CcpJsonRepresentation> getBotReplyEndMessages(String... stepNames) {
		String botReply = "{" + JnJsonInstantMessengerFields.botReply + "}";
		JnLanguage[] languages = JnLanguage.values();
		List<CcpJsonRepresentation> endMessages = new ArrayList<>();

		for (String stepName : stepNames) {
			for (JnLanguage language : languages) {
				CcpJsonRepresentation endMessage = getEndMessage(stepName, language, botReply);
				endMessages.add(endMessage);
			}
		}
		return endMessages;
	}

	/**
	 * Builds a text end message of the step in the language.
	 * @param stepName the step
	 * @param language the language
	 * @param message the text
	 * @return the record
	 */
	public static CcpJsonRepresentation getEndMessage(String stepName, JnLanguage language, String message) {
		CcpJsonRepresentation endMessageWithStepName = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.stepName, stepName);
		CcpJsonRepresentation endMessageWithLanguage = endMessageWithStepName.put(JnJsonCommonsFields.language, language);
		CcpJsonRepresentation endMessageWithMessage = endMessageWithLanguage.put(JnJsonCommonsFields.message, message);
		CcpJsonRepresentation endMessage = endMessageWithMessage.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text);
		return endMessage;
	}
}
