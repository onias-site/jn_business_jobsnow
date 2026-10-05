package com.jn.entities.decorators.enums;

import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType.copyDataTo;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType.transferDataTo;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase._after;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase._before;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase.mainEntity;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase.twinEntity;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.LENIENT;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.LOG;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.THROWS;
import static com.jn.business.messages.JnMessageType.email;
import static com.jn.business.messages.JnMessageType.instantMessenger;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;

/**
 * Covers every possible combination of the {@code operationPhase}, {@code transferType},
 * {@code entityPhase}, {@code messagesTypes} and {@code exceptionHandler} fields of
 * {@code @JnEntitySendMessageToUserWhenTransferOperation}. Each item encapsulates the values its
 * name expresses, so that the annotation declares a single constant instead of the five fields.
 *
 * <p>Each item name reads as a sentence: {@code [operationPhase][transferType]From
 * [entityPhase]Send[messagesTypes]AndIfFails[exceptionHandler]}.
 */
public enum JnEntitySendMessageToUserWhenTransferOperationType {
	/** After {@code transferDataTo} on the main entity, sends an e-mail; a failure throws the error. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, email),
	/** After {@code transferDataTo} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, email),
	/** After {@code transferDataTo} on the main entity, sends an e-mail; a failure is only logged. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, email),
	/** After {@code transferDataTo} on the main entity, sends an instant message; a failure throws the error. */
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, instantMessenger),
	/** After {@code transferDataTo} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, instantMessenger),
	/** After {@code transferDataTo} on the main entity, sends an instant message; a failure is only logged. */
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, instantMessenger),
	/** After {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, email, instantMessenger),
	/** After {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, email, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail; a failure throws the error. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, email),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, email),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail; a failure is only logged. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, email),
	/** After {@code transferDataTo} on the twin entity, sends an instant message; a failure throws the error. */
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an instant message; a failure is only logged. */
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, email, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, email, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an e-mail; a failure throws the error. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, email),
	/** After {@code copyDataTo} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, email),
	/** After {@code copyDataTo} on the main entity, sends an e-mail; a failure is only logged. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, email),
	/** After {@code copyDataTo} on the main entity, sends an instant message; a failure throws the error. */
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an instant message; a failure is only logged. */
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, email, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, email, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail; a failure throws the error. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, email),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, email),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail; a failure is only logged. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, email),
	/** After {@code copyDataTo} on the twin entity, sends an instant message; a failure throws the error. */
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an instant message; a failure is only logged. */
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, email, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, email, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail; a failure throws the error. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, email),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, email),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail; a failure is only logged. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, email),
	/** Before {@code transferDataTo} on the main entity, sends an instant message; a failure throws the error. */
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an instant message; a failure is recorded as a warning. */
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an instant message; a failure is only logged. */
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, email, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, email, instantMessenger),
	/** Before {@code transferDataTo} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, email, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail; a failure throws the error. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, email),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, email),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail; a failure is only logged. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, email),
	/** Before {@code transferDataTo} on the twin entity, sends an instant message; a failure throws the error. */
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an instant message; a failure is only logged. */
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, email, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, email, instantMessenger),
	/** Before {@code transferDataTo} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, email, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail; a failure throws the error. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, email),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, email),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail; a failure is only logged. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, email),
	/** Before {@code copyDataTo} on the main entity, sends an instant message; a failure throws the error. */
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an instant message; a failure is recorded as a warning. */
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an instant message; a failure is only logged. */
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, email, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, email, instantMessenger),
	/** Before {@code copyDataTo} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, email, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail; a failure throws the error. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, email),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, email),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail; a failure is only logged. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, email),
	/** Before {@code copyDataTo} on the twin entity, sends an instant message; a failure throws the error. */
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an instant message; a failure is only logged. */
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, email, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, email, instantMessenger),
	/** Before {@code copyDataTo} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, email, instantMessenger)
	;

	/**
	 * Execution moment: {@code _before} or {@code _after} the transfer.
	 */
	public final CcpEntityOperationPhase operationPhase;

	/**
	 * Transfer type: {@code copyDataTo} or {@code transferDataTo}.
	 */
	public final CcpEntityDecoratorTransferType transferType;

	/**
	 * Source entity (mainEntity or twinEntity).
	 */
	public final CcpEntityPhase entityPhase;

	/**
	 * Policy for handling a failure while sending the message.
	 */
	public final JnMessageSenderExceptionHandler exceptionHandler;

	/** The kinds of message sent (e-mail, instant message or both). */
	private final JnMessageType[] messagesTypes;

	/**
	 * Associates the item with the values its name expresses.
	 * @param operationPhase when the message is sent
	 * @param transferType the transfer
	 * @param entityPhase the source entity
	 * @param exceptionHandler what happens when the sending fails
	 * @param messagesTypes the kinds of message sent
	 */
	private JnEntitySendMessageToUserWhenTransferOperationType(
			CcpEntityOperationPhase operationPhase,
			CcpEntityDecoratorTransferType transferType,
			CcpEntityPhase entityPhase,
			JnMessageSenderExceptionHandler exceptionHandler,
			JnMessageType... messagesTypes) {
		this.operationPhase = operationPhase;
		this.transferType = transferType;
		this.entityPhase = entityPhase;
		this.exceptionHandler = exceptionHandler;
		this.messagesTypes = messagesTypes;
	}

	/**
	 * Message types to send. Never empty: every covered combination has at least one type.
	 */
	public JnMessageType[] messagesTypes() {
		JnMessageType[] copy = this.messagesTypes.clone();
		return copy;
	}
}

