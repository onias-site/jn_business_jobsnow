package com.jn.business.messages;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTextDecorator;
import com.jn.entities.JnEntitySupportCancelledCommand;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Called by the instant message sending right after a message is delivered: when it went to the support bot and
 * its text is a bot command, the operator has a ticket to solve, and the command is recorded in
 * {@link JnEntitySupportPendingCommand}. Any other message (to the user bot, or a notice to support that is not a
 * command, like the error files) is left alone.
 *
 * <p>The text recorded is the one the operator received: the template of the message resolved with the values of
 * this sending, the same resolution {@link JnInstantMessageType} applies before delivering it.
 */
public class JnBusinessRegisterSupportPendingCommand implements CcpBusiness{

	/** The single instance. */
	public static final JnBusinessRegisterSupportPendingCommand INSTANCE = new JnBusinessRegisterSupportPendingCommand();

	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessRegisterSupportPendingCommand() {}

	/**
	 * Records the command sent to the support bot (normalized, with the chat id and the current time) as pending.
	 * @param json the instant message just delivered
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String botName = json.getAsString(JnJsonInstantMessengerFields.botName);
		String supportBotName = JnMessageType.JnBotType.support.name();
		boolean toAnotherBot = false == supportBotName.equals(botName);

		if(toAnotherBot) {
			return json;
		}

		CcpTextDecorator template = json.getAsTextDecorator(JnJsonCommonsFields.message);
		CcpTextDecorator resolvedTemplate = template.resolveTemplate(json);
		String sentText = resolvedTemplate.content;
		boolean isCommand = JnEntitySupportPendingCommand.isCommand(sentText);

		if(false == isCommand) {
			return json;
		}

		String command = JnEntitySupportPendingCommand.normalize(sentText);
		Long chatId = json.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
		long now = System.currentTimeMillis();

		CcpJsonRepresentation pendingCommandWithBotName = CcpOtherConstants.EMPTY_JSON.put(JnEntitySupportPendingCommand.Fields.botName, botName);
		CcpJsonRepresentation pendingCommandWithChatId = pendingCommandWithBotName.put(JnEntitySupportPendingCommand.Fields.chatId, chatId);
		CcpJsonRepresentation pendingCommandWithCommand = pendingCommandWithChatId.put(JnEntitySupportPendingCommand.Fields.command, command);
		CcpJsonRepresentation pendingCommand = pendingCommandWithCommand.put(JnEntitySupportPendingCommand.Fields.timestamp, now);

		JnEntitySupportPendingCommand.ENTITY.save(pendingCommand);
		// sent again after the user gave it up and before the operator listed the tickets: the cancellation is older
		// than this ticket and must not delete it
		JnEntitySupportCancelledCommand.ENTITY.delete(pendingCommand);
		return json;
	}
}
