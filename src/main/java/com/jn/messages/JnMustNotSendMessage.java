package com.jn.messages;

import java.lang.reflect.Field;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.entities.JnEntityMessageDidNotSent.JnReasonDetails;

/**
 * The "do not send" rules of {@link JnSendMessageToUser}, one per list of entities of the sender (same name). Each rule
 * checks the entity of the channel in the search result: a rule marked "when present" refuses the sending when the
 * record exists, the others refuse it when the record is missing. A refusal is recorded in
 * {@code jn_message_did_not_sent} (when the message has a receiver) and raised as {@link MessageDidNotSend}. A refusal
 * chosen by the receiver (the e-mail provider suppressed the address, the user blocked the bot) only skips its channel,
 * whatever the exception handler of the channel: it is not a failure of the system.
 */
public enum JnMustNotSendMessage{

	/** Refuses when the same message was already sent within the window of the entity. */
	alreadySentEntities(true, false),
	/** Refuses when the channel has no sending parameters. */
	parameterEntities(false, false),
	/** Refuses when the channel has no template. */
	messageEntities(false, false),
	/**
	 * Refuses when the receiver blocked the channel (address suppressed by the e-mail provider, bot locked). Up to
	 * 2026-11-12 a channel with {@code THROWS} raised this refusal: a contact us from a suppressed address failed after
	 * saving the ticket, and the notice to the support team, the next channel, was never sent.
	 */
	blockEntities(true, true)
	;
	/** Whether the rule refuses when the record exists ({@code true}) or when it is missing ({@code false}). */
	final boolean whenPresentInUnionAll;

	/** Whether the refusal was chosen by the receiver, so it only skips the channel and never reaches its handler. */
	final boolean refusalChosenByTheReceiver;
	
	/**
	 * Associates the rule with its condition.
	 * @param whenPresentInThisUnionAll whether the rule refuses when the record exists
	 * @param refusalChosenByTheReceiver whether the refusal was chosen by the receiver
	 */
	private JnMustNotSendMessage(boolean whenPresentInThisUnionAll, boolean refusalChosenByTheReceiver) {
		this.whenPresentInUnionAll = whenPresentInThisUnionAll;
		this.refusalChosenByTheReceiver = refusalChosenByTheReceiver;
	}
	
	/**
	 * Reads, by reflection, the list of entities of the sender with the name of this rule.
	 * @param obj the sender
	 * @return the entities by channel
	 * @throws JnErrorMessageEntitiesNotAccessible when the list can not be read
	 */
	@SuppressWarnings("unchecked")
	protected List<CcpEntity> getEntities(JnSendMessageToUser obj){
		try {
			String name = this.name();
			Field declaredField = JnSendMessageToUser.class.getDeclaredField(name);
			declaredField.setAccessible(true);
			var get = declaredField.get(obj);
			List<CcpEntity> listCcpEntity = (List<CcpEntity>)get;
			return listCcpEntity;
			
		} catch (Exception e) {
			JnErrorMessageEntitiesNotAccessible jnErrorMessageEntitiesNotAccessible = new JnErrorMessageEntitiesNotAccessible(this, e);
			throw jnErrorMessageEntitiesNotAccessible;
		}
	}

	/**
	 * Raised when the list of entities of a rule can not be read by reflection from {@code JnSendMessageToUser}, which means
	 * the field was renamed or removed.
	 */
	@SuppressWarnings("serial")
	public static class JnErrorMessageEntitiesNotAccessible extends RuntimeException {
		/**
		 * Names the field that could not be read and chains the reflection error.
		 * @param field the rule whose field of the same name was expected in {@code JnSendMessageToUser}
		 * @param cause the reflection error
		 */
		private JnErrorMessageEntitiesNotAccessible(JnMustNotSendMessage field, Throwable cause) {
			super("The field '" + field + "' could not be read from JnSendMessageToUser", cause);
		}
	}
	
	/**
	 * Records the refusal (reason details, the checked entity as the reason type, the rule as the reason description and the
	 * reason message) and raises it.
	 * @param obj the sender
	 * @param unionAll the search result
	 * @param json the keys of the channel
	 * @param index the position of the channel
	 * @param reasonDetails the details of the reason
	 * @param reasonMessage the message of the reason, empty when there is none
	 * @throws MessageDidNotSend always
	 */
	private void saveMessageNotSent(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, 
			CcpJsonRepresentation json, Integer index, 
			JnReasonDetails reasonDetails, String reasonMessage) {
		
		List<CcpEntity> entities = this.getEntities(obj);
		CcpEntity entity = entities.get(index);
		CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
		String reasonDescription = this.name();
		CcpJsonRepresentation put = json
				.put(JnEntityMessageDidNotSent.Fields.reasonDetails,  reasonDetails)
				.put(JnEntityMessageDidNotSent.Fields.reasonType,  entityMetaData.entityName)
				.put(JnEntityMessageDidNotSent.Fields.reasonDescription,  reasonDescription)
				;
		CcpJsonRepresentation jsonToSave = this.putReasonMessage(put, reasonMessage);
		this.saveDiagnosticWhenItHasARecipient(jsonToSave);
		throw new MessageDidNotSend(this, jsonToSave);
	}

	/**
	 * The diagnosis is keyed by the receiver: {@code email} is part of the primary key of {@code JnEntityMessageDidNotSent}.
	 * A notice to the support team has no receiver e-mail: saving it made the validation of the entity fail and replaced the
	 * expected refusal with a validation error. Without a receiver nobody needs the diagnosis, and the refusal is still
	 * raised.
	 * @param jsonToSave the diagnosis
	 */
	private void saveDiagnosticWhenItHasARecipient(CcpJsonRepresentation jsonToSave) {

		CcpEntityMetaData didNotSentMetaData = JnEntityMessageDidNotSent.ENTITY.getEntityMetaData();

		boolean hasNoRecipient = false == jsonToSave.containsAllFields(didNotSentMetaData.primaryKeyNames);

		if(hasNoRecipient) {
			return;
		}

		JnEntityMessageDidNotSent.ENTITY.save(jsonToSave);
	}

	/**
	 * The field is optional in the entity, which on the other hand rejects an empty text. When the reason has no message
	 * (a record simply missing from the search), the field is removed from the JSON, so the validation does not reject the
	 * diagnosis.
	 * @param json the diagnosis
	 * @param reasonMessage the message of the reason
	 * @return the diagnosis with or without the message
	 */
	private CcpJsonRepresentation putReasonMessage(CcpJsonRepresentation json, String reasonMessage) {

		boolean thereIsNoReasonMessage = reasonMessage.trim().isEmpty();

		if(thereIsNoReasonMessage) {
			CcpJsonRepresentation removeFields = json.removeFields(JnEntityMessageDidNotSent.Fields.reasonMessage);
			return removeFields;
		}

		CcpJsonRepresentation put = json.put(JnEntityMessageDidNotSent.Fields.reasonMessage, reasonMessage);
		return put;
	}
	
	/** Raised when a rule refuses the sending; the message is the recorded diagnosis. */
	@SuppressWarnings("serial")
	static class MessageDidNotSend extends RuntimeException{

		/** The rule that refused the sending. */
		final JnMustNotSendMessage rule;

		/**
		 * Builds the refusal.
		 * @param rule the rule that refused the sending
		 * @param json the diagnosis
		 */
		protected MessageDidNotSend(JnMustNotSendMessage rule, CcpJsonRepresentation json) {
			super(json.toString());
			this.rule = rule;
		}
	}
	
	/**
	 * Applies the rule to the channel. When the primary key of the checked entity can not be computed, the sending is
	 * refused with {@code missingFieldsToPrimaryKey}.
	 * @param obj the sender
	 * @param unionAll the search result
	 * @param json the keys of the channel
	 * @param index the position of the channel
	 * @throws MessageDidNotSend when the sending is refused
	 */
	public void validate(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, CcpJsonRepresentation json, Integer index) {
		
		JnReasonDetails reasonDetails = JnReasonDetails.isNotPresentInThisUnionAll;
		
		String reasonMessage = "";
		
		List<CcpEntity> entities = this.getEntities(obj);
		
		CcpEntity entity = entities.get(index);
		
		try {
			boolean isPresentInThisUnionAll  = entity.isPresentInThisUnionAll(unionAll, json);

			if(isPresentInThisUnionAll) {
				reasonDetails = JnReasonDetails.isPresentInThisUnionAll;
			}
			
			boolean mustSendMessage = this.whenPresentInUnionAll != isPresentInThisUnionAll;
			
			if(mustSendMessage) {
				return;
			}
			
		} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
			reasonDetails = JnReasonDetails.missingFieldsToPrimaryKey;
			reasonMessage = e.getMessage();
		}
	
		this.saveMessageNotSent(obj, unionAll, json, index, reasonDetails, reasonMessage);
	
	}

}
