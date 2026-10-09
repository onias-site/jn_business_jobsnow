package com.jn.business.messages;

import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntitySupportCancelledCommand;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Called when the user gives up what a command sent to the support operator was about (a suggestion withdrawn): the
 * ticket that command became must leave the list of the operator. The command is rebuilt the way it was sent, from the
 * instant message template and sending parameters of the {@code templateId} resolved with the values of the
 * request ({@code JnSendMessageToUser} does the same before sending), and recorded in
 * {@link JnEntitySupportCancelledCommand}, which the support bot applies when it lists the tickets. The inbox record
 * ({@link JnEntitySupportPendingCommand}) is deleted at once; the bot may already have moved it to its own list, which
 * this cost center can not see, hence the cancellation.
 */
public class JnBusinessCancelSupportPendingCommand implements CcpBusiness {

	/** The single instance. */
	public static final JnBusinessCancelSupportPendingCommand INSTANCE = new JnBusinessCancelSupportPendingCommand();

	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessCancelSupportPendingCommand() {}

	/**
	 * Cancels the ticket of the command sent to the support bot by the template, if that template sends one.
	 * @param templateId the template of the notice that carried the command
	 * @param values the values the notice was resolved with (the readable email included)
	 */
	public void cancel(String templateId, CcpJsonRepresentation values) {
		CcpJsonRepresentation valuesWithTemplateId = values.put(JnJsonCommonsFields.templateId, templateId);
		this.execute(valuesWithTemplateId);
	}

	/**
	 * Cancels the ticket of the command sent to the support bot by the {@code templateId} of the json.
	 * @param json the values the notice was resolved with, plus the {@code templateId}
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation keys = JnMessageType.instantMessenger.getParameters(json);

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll sendingRecords = crud.unionAll(keys, JnDeleteKeysFromCache.INSTANCE, JnEntityInstantMessengerParametersToSend.ENTITY, JnEntityInstantMessengerTemplateMessage.ENTITY);
		Supplier<CcpJsonRepresentation> keysSupplier = keys.getJsonSupplier();
		CcpJsonRepresentation template = JnEntityInstantMessengerTemplateMessage.ENTITY.getRecordFromUnionAll(sendingRecords, keysSupplier);
		CcpJsonRepresentation parameters = JnEntityInstantMessengerParametersToSend.ENTITY.getRecordFromUnionAll(sendingRecords, keysSupplier);

		boolean noInstantMessage = template.isEmpty() || parameters.isEmpty();

		if(noInstantMessage) {
			return json;
		}

		CcpTextDecorator message = template.getAsTextDecorator(JnJsonCommonsFields.message);
		CcpTextDecorator resolvedMessage = message.resolveTemplate(keys);
		String sentText = resolvedMessage.content;
		boolean notACommand = false == JnEntitySupportPendingCommand.isCommand(sentText);

		if(notACommand) {
			return json;
		}

		String command = JnEntitySupportPendingCommand.normalize(sentText);
		String botName = parameters.getAsString(JnJsonInstantMessengerFields.botName);
		Long chatId = parameters.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
		long now = System.currentTimeMillis();

		CcpJsonRepresentation commandWithBotName = CcpOtherConstants.EMPTY_JSON.put(JnEntitySupportCancelledCommand.Fields.botName, botName);
		CcpJsonRepresentation commandWithChatId = commandWithBotName.put(JnEntitySupportCancelledCommand.Fields.chatId, chatId);
		CcpJsonRepresentation commandWithCommand = commandWithChatId.put(JnEntitySupportCancelledCommand.Fields.command, command);
		CcpJsonRepresentation cancelledCommand = commandWithCommand.put(JnEntitySupportCancelledCommand.Fields.timestamp, now);

		JnEntitySupportPendingCommand.ENTITY.delete(cancelledCommand);
		JnEntitySupportCancelledCommand.ENTITY.save(cancelledCommand);
		return json;
	}
}
