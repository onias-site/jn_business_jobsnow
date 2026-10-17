package com.jn.entities;

import java.util.Arrays;
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
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.subfields.JnNextStepFields;
import com.jn.entities.subfields.JnNextStepMessageFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * A step of a bot command: the business it runs ({@code engine}), the step that follows ({@code nextStep}) and, by status
 * of a {@code CcpErrorFlowDisturb} thrown by the engine, the messages to send and the step to go to ({@code stepFlow}).
 * The bot instantiates the engine by its class name, so the cost center that owns a command seeds its steps with its own
 * classes and the bot never imports them (the same way {@code CcpMensageriaReceiver} runs the topic of a message).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_bot_command_step}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JnEntityBotCommandStep.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityBotCommandStep implements CcpEntityConfigurator {

	/** The entity {@code jn_bot_command_step}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityBotCommandStep.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code stepName} field: part of the primary key, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		stepName,
		/** The {@code stepFlow} field: required, list, nested JSON. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray(minSize = 1)
		@CcpJsonFieldTypeNestedJson(jsonValidation = JnNextStepFields.class)
		stepFlow,
		/** The {@code engine} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		engine,
		/** The {@code nextStep} field: text. */
		@CcpJsonFieldTypeString
		nextStep,
		;
	}


	/**
	 * Builds a complete record of this entity, ready for {@code ENTITY.save(...)}. The {@code stepFlow} items come from
	 * {@link #getStepFlow(Integer, String, CcpJsonRepresentation...)} and there must be at least one, as
	 * {@code @CcpJsonFieldValidatorArray(minSize = 1)} requires. {@code nextStep} is left out of the JSON when blank, by the
	 * same criterion as {@link #getStepFlow(Integer, String, CcpJsonRepresentation...)}.
	 * @param stepName the name of the step
	 * @param engine the business run by the step
	 * @param nextStep the step that follows, blank for none
	 * @param stepFlow the flow items
	 * @return the record
	 */
	public static CcpJsonRepresentation getBotCommandStep(String stepName, Class<?> engine, String nextStep, CcpJsonRepresentation... stepFlow) {

		String engineName = engine.getName();
		List<CcpJsonRepresentation> stepFlowList = Arrays.asList(stepFlow);

		CcpJsonRepresentation putStepName = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.stepName, stepName);
		CcpJsonRepresentation putEngine = putStepName.put(Fields.engine, engineName);
		CcpJsonRepresentation botCommandStep = putEngine.put(Fields.stepFlow, stepFlowList);

		String nextStepTrim = nextStep.trim();
		boolean hasNextStep = false == nextStepTrim.isEmpty();

		if(hasNextStep) {
			botCommandStep = botCommandStep.put(Fields.nextStep, nextStep);
		}

		return botCommandStep;
	}

	/**
	 * Builds a {@code stepFlow} item, in the format of {@code JnNextStepFields}: the status that triggers the diversion, the
	 * alternative step and the messages sent to the user. The messages come from
	 * {@link #getStepFlowMessage(JnLanguage, String)}. Both {@code message} (empty varargs) and {@code nextStep} (blank) are
	 * left out of the JSON: writing them empty would break the {@code @CcpJsonFieldValidatorArray(minSize = 1)} of the first
	 * and wrongly satisfy the {@code requiresAtLeastOne} between the two.
	 * @param status the status
	 * @param nextStep the alternative step, blank for none
	 * @param message the messages
	 * @return the flow item
	 */
	public static CcpJsonRepresentation getStepFlow(Integer status, String nextStep, CcpJsonRepresentation... message) {

		List<CcpJsonRepresentation> messageList = Arrays.asList(message);

		CcpJsonRepresentation stepFlow = CcpOtherConstants.EMPTY_JSON .put(JnNextStepFields.status, status);

		String nextStepTrim = nextStep.trim();
		boolean hasNextStep = false == nextStepTrim.isEmpty();

		if(hasNextStep) {
			stepFlow = stepFlow.put(JnNextStepFields.nextStep, nextStep);
		}

		boolean hasMessage = false == messageList.isEmpty();

		if(hasMessage) {
			stepFlow = stepFlow.put(JnNextStepFields.message, messageList);
		}

		return stepFlow;
	}

	/**
	 * Builds a message of a {@code stepFlow} item, in the format of {@code JnNextStepMessageFields}: the language and the
	 * text sent to the user.
	 * @param language the language
	 * @param message the text
	 * @return the message
	 */
	public static CcpJsonRepresentation getStepFlowMessage(JnLanguage language, String message) {

		String languageName = language.name();

		CcpJsonRepresentation putLanguage = CcpOtherConstants.EMPTY_JSON.put(JnNextStepMessageFields.language, languageName);
		CcpJsonRepresentation stepFlowMessage = putLanguage.put(JnNextStepMessageFields.message, message);

		return stepFlowMessage;
	}

	/**
	 * The flow messages of the steps whose engine already writes the reply in the language of the session: the text the
	 * engine left in {@code botReply}, whatever the language.
	 * @return the message in every language
	 */
	public static CcpJsonRepresentation[] getBotReplyInEveryLanguage() {
		String botReply = "{" + JnJsonInstantMessengerFields.botReply + "}";
		CcpJsonRepresentation portuguese = getStepFlowMessage(JnLanguage.portuguese, botReply);
		CcpJsonRepresentation english = getStepFlowMessage(JnLanguage.english, botReply);
		CcpJsonRepresentation spanish = getStepFlowMessage(JnLanguage.spanish, botReply);
		CcpJsonRepresentation[] botReplyInEveryLanguage = {portuguese, english, spanish};
		return botReplyInEveryLanguage;
	}
}
