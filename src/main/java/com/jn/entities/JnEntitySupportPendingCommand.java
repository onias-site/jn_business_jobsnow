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
 * Inbox of the bot commands sent to the support operator (for example {@code /solveLoginTokenTicket unlockToken <email>} or {@code /fixSkillHierarchy <parent> <type> <email>}) that the operator has not run yet. Each record is written by the instant message sending ({@code JnBusinessRegisterSupportPendingCommand}) as soon as the command reaches the operator, whatever the cost center that triggered it, so a new kind of ticket needs no registration here: it is enough that its message to the support bot is a command. The support bot itself belongs to the jb cost center, which jn cannot see; this entity is where the two meet. The bot moves these records to its own list of tickets and deletes them when the operator runs the command. No cache: the record is written by one process and read by another.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_support_pending_command}</li>
 * </ul>
 */
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySupportPendingCommand.Fields.class)
public class JnEntitySupportPendingCommand implements CcpEntityConfigurator {

	/** The entity {@code jn_support_pending_command}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySupportPendingCommand.class).entityInstance;

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

		/** The {@code command} field: part of the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		command,

		/** The {@code timestamp} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp,
		;
	}

	/**
	 * Whether the text is a bot command: it starts with a slash.
	 */
	public static boolean isCommand(String text) {
		String trimmedText = text.trim();
		boolean command = trimmedText.startsWith("/");
		return command;
	}

	/**
	 * The command as it identifies a ticket: without the spaces around it and with a single space between its
	 * words. The template of a notice may leave a trailing space, and the operator may type two spaces between
	 * the parameters; both still name the same ticket.
	 */
	public static String normalize(String command) {
		String trimmedCommand = command.trim();
		String normalizedCommand = trimmedCommand.replaceAll("\\s+", " ");
		return normalizedCommand;
	}
}
