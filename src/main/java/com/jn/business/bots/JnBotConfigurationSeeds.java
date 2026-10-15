package com.jn.business.bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandExplanation;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.messages.JnSystemMessage;
import com.jn.utils.JnLanguage;

/**
 * Builds the seed records of the commands that a cost center adds to a bot: the command and its parameters
 * ({@link JnEntityBotCommand}), its steps ({@link JnEntityBotCommandStep}), the end messages of the steps
 * ({@link JnEntityBotCommandStepEndMessage}), the names in Portuguese ({@link JnEntityBotCommandName}), the explanations
 * ({@link JnEntityBotCommandExplanation}) and the texts of the conversation ({@code JnEntitySystemMessage}). The owner of
 * the commands returns {@link #getBulkItems()} from the {@code getFirstRecordsToInsert} of one of its entities, so the bot
 * engine (jb) finds the commands of vis without importing vis. Immutable: every method returns a new instance with the
 * records added.
 */
public final class JnBotConfigurationSeeds {

	/** The bot that runs the commands. */
	private final String botName;

	/** The records so far. */
	private final List<CcpBulkItem> bulkItems;

	/**
	 * Starts the seeds of the commands of a bot.
	 * @param botName the bot that runs the commands
	 */
	public JnBotConfigurationSeeds(CcpJsonFieldName botName) {
		this(botName.name(), Collections.emptyList());
	}

	/**
	 * Keeps the state.
	 * @param botName the bot that runs the commands
	 * @param bulkItems the records so far
	 */
	private JnBotConfigurationSeeds(String botName, List<CcpBulkItem> bulkItems) {
		this.botName = botName;
		this.bulkItems = Collections.unmodifiableList(bulkItems);
	}

	/**
	 * Adds the records of the entity.
	 * @param entity the entity
	 * @param records the records
	 * @return the seeds with the records added
	 */
	private JnBotConfigurationSeeds add(CcpEntity entity, List<CcpJsonRepresentation> records) {
		List<CcpBulkItem> newBulkItems = new ArrayList<>(this.bulkItems);
		for (CcpJsonRepresentation record : records) {
			List<CcpBulkItem> recordBulkItems = entity.toBulkItems(record, CcpBulkEntityOperationType.create);
			newBulkItems.addAll(recordBulkItems);
		}
		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(this.botName, newBulkItems);
		return seeds;
	}

	/**
	 * Adds bulk items already built.
	 * @param moreBulkItems the bulk items
	 * @return the seeds with the bulk items added
	 */
	private JnBotConfigurationSeeds addBulkItems(List<CcpBulkItem> moreBulkItems) {
		List<CcpBulkItem> newBulkItems = new ArrayList<>(this.bulkItems);
		newBulkItems.addAll(moreBulkItems);
		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(this.botName, newBulkItems);
		return seeds;
	}

	/**
	 * Adds a command of the bot, with the names of its parameters in the order the operator types them (the last one
	 * takes the rest of the text, so it is the one that may have spaces).
	 * @param commandName the canonical name of the command
	 * @param parameterNames the parameters, none for a command without parameters
	 * @return the seeds with the command added
	 */
	public JnBotConfigurationSeeds command(String commandName, CcpJsonFieldName... parameterNames) {
		CcpJsonRepresentation commandWithName = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.commandName, commandName);
		CcpJsonRepresentation command = commandWithName.put(JnJsonInstantMessengerFields.botName, this.botName);
		boolean hasNoParameters = parameterNames.length == 0;
		if(hasNoParameters) {
			List<CcpJsonRepresentation> records = Arrays.asList(command);
			JnBotConfigurationSeeds seeds = this.add(JnEntityBotCommand.ENTITY, records);
			return seeds;
		}
		Stream<CcpJsonFieldName> parameterNamesStream = Arrays.stream(parameterNames);
		Stream<String> parameterNamesAsText = parameterNamesStream.map(parameterName -> parameterName.name());
		List<String> parameters = parameterNamesAsText.collect(Collectors.toList());
		CcpJsonRepresentation commandWithParameters = command.put(JnEntityBotCommand.Fields.parameterName, parameters);
		List<CcpJsonRepresentation> records = Arrays.asList(commandWithParameters);
		JnBotConfigurationSeeds seeds = this.add(JnEntityBotCommand.ENTITY, records);
		return seeds;
	}

	/**
	 * Adds steps, built by {@code JnEntityBotCommandStep.getBotCommandStep}.
	 * @param steps the steps
	 * @return the seeds with the steps added
	 */
	public JnBotConfigurationSeeds steps(CcpJsonRepresentation... steps) {
		List<CcpJsonRepresentation> records = Arrays.asList(steps);
		JnBotConfigurationSeeds seeds = this.add(JnEntityBotCommandStep.ENTITY, records);
		return seeds;
	}

	/**
	 * Adds end messages of steps, built by the static methods of {@link JnEntityBotCommandStepEndMessage}.
	 * @param endMessages the end messages
	 * @return the seeds with the end messages added
	 */
	public JnBotConfigurationSeeds endMessages(List<CcpJsonRepresentation> endMessages) {
		JnBotConfigurationSeeds seeds = this.add(JnEntityBotCommandStepEndMessage.ENTITY, endMessages);
		return seeds;
	}

	/**
	 * Adds the names in Portuguese of commands: the name of each item is the canonical name of the command and
	 * {@code getValue()} its name in Portuguese.
	 * @param commandNamesInPortuguese the commands
	 * @return the seeds with the names added
	 */
	public JnBotConfigurationSeeds portugueseNames(CcpJsonFieldName... commandNamesInPortuguese) {
		List<CcpBulkItem> portugueseNames = JnEntityBotCommandName.getPortugueseNames(commandNamesInPortuguese);
		JnBotConfigurationSeeds seeds = this.addBulkItems(portugueseNames);
		return seeds;
	}

	/**
	 * Adds the explanation of a command in a language.
	 * @param commandName the canonical name of the command
	 * @param language the language
	 * @param explanation the text
	 * @return the seeds with the explanation added
	 */
	public JnBotConfigurationSeeds commandExplanation(String commandName, JnLanguage language, String explanation) {
		CcpJsonRepresentation explanationWithCommand = CcpOtherConstants.EMPTY_JSON.put(JnJsonInstantMessengerFields.commandName, commandName);
		CcpJsonRepresentation explanationWithLanguage = explanationWithCommand.put(JnJsonCommonsFields.language, language.name());
		CcpJsonRepresentation record = explanationWithLanguage.put(JnJsonInstantMessengerFields.message, explanation);
		List<CcpJsonRepresentation> records = Arrays.asList(record);
		JnBotConfigurationSeeds seeds = this.add(JnEntityBotCommandExplanation.ENTITY, records);
		return seeds;
	}

	/**
	 * Adds the Portuguese and English records of a text of the conversation.
	 * @param systemMessage the text
	 * @param portugueseText the Portuguese text
	 * @param englishText the English text
	 * @return the seeds with the text added
	 */
	public JnBotConfigurationSeeds systemMessage(JnSystemMessage systemMessage, String portugueseText, String englishText) {
		List<CcpBulkItem> inPortuguese = systemMessage.toBulkItems(JnLanguage.portuguese, portugueseText);
		List<CcpBulkItem> inEnglish = systemMessage.toBulkItems(JnLanguage.english, englishText);
		JnBotConfigurationSeeds withPortuguese = this.addBulkItems(inPortuguese);
		JnBotConfigurationSeeds seeds = withPortuguese.addBulkItems(inEnglish);
		return seeds;
	}

	/**
	 * Adds bulk items of other entities that belong to the same configuration (e.g. message templates).
	 * @param moreBulkItems the bulk items
	 * @return the seeds with the bulk items added
	 */
	public JnBotConfigurationSeeds bulkItems(List<CcpBulkItem> moreBulkItems) {
		JnBotConfigurationSeeds seeds = this.addBulkItems(moreBulkItems);
		return seeds;
	}

	/**
	 * Returns the records, to be returned by {@code getFirstRecordsToInsert}.
	 * @return the bulk items
	 */
	public List<CcpBulkItem> getBulkItems() {
		return this.bulkItems;
	}
}
