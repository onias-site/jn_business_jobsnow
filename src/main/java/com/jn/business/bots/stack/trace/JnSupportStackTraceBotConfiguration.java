package com.jn.business.bots.stack.trace;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.jn.business.bots.JnBotConfigurationSeeds;
import com.jn.business.bots.JnCommandNamesInPortuguese;
import com.jn.business.bots.JnSupportBotCommands;
import com.jn.business.messages.JnMessageType;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.utils.JnLanguage;

/**
 * Configuration of {@code /solveStackTrace <errorId>} in the support bot: shows an error that no one handled, and the
 * operator closes the ticket or leaves it open to go to the next one. Seeded by {@code JnEntityJobsnowError}.
 */
public final class JnSupportStackTraceBotConfiguration {

	private JnSupportStackTraceBotConfiguration() {}

	/**
	 * The seed records of the command.
	 * @return the bulk items
	 */
	public static List<CcpBulkItem> getSeeds() {
		String commandName = JnSupportBotCommands.solveStackTrace.name();
		String decide = JnSupportStackTraceSteps.solveStackTraceDecide.name();
		CcpJsonRepresentation[] botReply = JnEntityBotCommandStep.getBotReplyInEveryLanguage();

		// an id that does not exist ends the session with that notice
		int errorNotFound = JnSupportStackTraceStatus.errorNotFound.asNumber();
		CcpJsonRepresentation errorNotFoundFlow = JnEntityBotCommandStep.getStepFlow(errorNotFound, "", botReply);
		CcpJsonRepresentation showStep = JnEntityBotCommandStep.getBotCommandStep(commandName, JnSupportStackTraceShow.class, decide, errorNotFoundFlow);

		// close the ticket or go to the next one; an answer not understood repeats the step
		int invalidAnswer = JnSupportStackTraceStatus.invalidAnswer.asNumber();
		int ticketSolved = JnSupportStackTraceStatus.ticketSolved.asNumber();
		int ticketSkipped = JnSupportStackTraceStatus.ticketSkipped.asNumber();
		CcpJsonRepresentation invalidAnswerFlow = JnEntityBotCommandStep.getStepFlow(invalidAnswer, decide, botReply);
		CcpJsonRepresentation ticketSolvedFlow = JnEntityBotCommandStep.getStepFlow(ticketSolved, "", botReply);
		CcpJsonRepresentation ticketSkippedFlow = JnEntityBotCommandStep.getStepFlow(ticketSkipped, "", botReply);
		CcpJsonRepresentation decideStep = JnEntityBotCommandStep.getBotCommandStep(decide, JnSupportStackTraceDecide.class, "", invalidAnswerFlow, ticketSolvedFlow, ticketSkippedFlow);

		List<CcpJsonRepresentation> endMessages = JnEntityBotCommandStepEndMessage.getBotReplyEndMessages(commandName, decide);

		JnBotConfigurationSeeds seeds = new JnBotConfigurationSeeds(JnMessageType.JnBotType.support)
				.command(commandName, JnSupportStackTraceFields.errorId)
				.portugueseNames(JnCommandNamesInPortuguese.solveStackTrace)
				.steps(showStep, decideStep)
				.endMessages(endMessages)
				.commandExplanation(commandName, JnLanguage.portuguese, "Quando um erro não tratado acontece no sistema, o operador de suporte evoca este comando para ver o erro e dizer que ele foi resolvido, ou deixar o ticket aberto e ir para o próximo")
				.commandExplanation(commandName, JnLanguage.english, "When an unhandled error happens in the system, the support operator invokes this command to see the error and tell that it was solved, or leave the ticket open and go to the next one")
				.systemMessage(JnSupportStackTraceMessages.ticket
						, "Erro não tratado\n\n"
						+ "Origem: {origin}\n"
						+ "Tipo: {type}\n"
						+ "Mensagem: {message}\n\n"
						+ "Stack trace:\n{stackTrace}\n\n"
						+ "Causa: {cause}\n\n"
						+ "{options}"
						, "Unhandled error\n\n"
						+ "Origin: {origin}\n"
						+ "Type: {type}\n"
						+ "Message: {message}\n\n"
						+ "Stack trace:\n{stackTrace}\n\n"
						+ "Cause: {cause}\n\n"
						+ "{options}")
				.systemMessage(JnSupportStackTraceMessages.options
						, "Digite \"1\" se o erro foi resolvido ou \"2\" para deixar este ticket aberto e ir para o próximo"
						, "Type \"1\" if the error was solved or \"2\" to leave this ticket open and go to the next one")
				.systemMessage(JnSupportStackTraceMessages.notUnderstood
						, "Não entendi a resposta.\n\n{options}"
						, "I did not understand the answer.\n\n{options}")
				.systemMessage(JnSupportStackTraceMessages.ticketSolved
						, "O erro {errorId} foi marcado como resolvido."
						, "The error {errorId} was marked as solved.")
				.systemMessage(JnSupportStackTraceMessages.ticketSkipped
						, "O ticket do erro {errorId} continua aberto."
						, "The ticket of the error {errorId} stays open.")
				.systemMessage(JnSupportStackTraceMessages.errorNotFound
						, "Não existe o erro {errorId}."
						, "There is no error {errorId}.");
		List<CcpBulkItem> bulkItems = seeds.getBulkItems();
		return bulkItems;
	}
}
