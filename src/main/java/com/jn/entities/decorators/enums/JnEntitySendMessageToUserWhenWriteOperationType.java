package com.jn.entities.decorators.enums;

import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.delete;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.deleteAnyWhere;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.insert;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.save;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.update;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase._after;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase._before;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase.mainEntity;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase.twinEntity;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.LENIENT;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.LOG;
import static com.jn.business.messages.JnMessageSenderExceptionHandler.THROWS;
import static com.jn.business.messages.JnMessageType.email;
import static com.jn.business.messages.JnMessageType.instantMessenger;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;

/**
 * Covers every possible combination of the {@code operationPhase}, {@code operationType},
 * {@code entityPhase}, {@code messagesTypes} and {@code exceptionHandler} fields of
 * {@code @JnEntitySendMessageToUserWhenWriteOperation}. Each item encapsulates the values its
 * name expresses, so that the annotation declares a single constant instead of the five fields.
 *
 * <p>Each item name reads as a sentence: {@code [operationPhase][operationType]From
 * [entityPhase]Send[messagesTypes]AndIfFails[exceptionHandler]}.
 *
 * <p>{@code afterSave} fires on any {@code save}, regardless of the return value;
 * {@code afterInsert} only when {@code save} returns {@code true} and {@code afterUpdate} only when
 * it returns {@code false}. There is no {@code beforeInsert} or {@code beforeUpdate}, because before
 * {@code save} the return value is not known yet.
 */
public enum JnEntitySendMessageToUserWhenWriteOperationType {
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, email),
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, email),
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, email),
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, instantMessenger),
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, instantMessenger),
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, instantMessenger),
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, email, instantMessenger),
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, email, instantMessenger),
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, email, instantMessenger),
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, email),
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, email),
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, email),
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, instantMessenger),
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, instantMessenger),
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, instantMessenger),
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, email, instantMessenger),
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, email, instantMessenger),
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, email, instantMessenger),
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, email),
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, email),
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, email),
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, instantMessenger),
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, instantMessenger),
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, instantMessenger),
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, email, instantMessenger),
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, email, instantMessenger),
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, email, instantMessenger),
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, email),
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, email),
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, email),
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, instantMessenger),
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, instantMessenger),
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, instantMessenger),
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, email, instantMessenger),
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, email, instantMessenger),
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, email, instantMessenger),
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, email),
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, email),
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, email),
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, instantMessenger),
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, instantMessenger),
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, instantMessenger),
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, email, instantMessenger),
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, email, instantMessenger),
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, email, instantMessenger),
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, email),
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, email),
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, email),
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, instantMessenger),
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, instantMessenger),
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, instantMessenger),
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, email, instantMessenger),
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, email, instantMessenger),
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, email, instantMessenger),
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, email),
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, email),
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, email),
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, instantMessenger),
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, instantMessenger),
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, instantMessenger),
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, email, instantMessenger),
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, email, instantMessenger),
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, email, instantMessenger),
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, email),
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, email),
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, email),
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, instantMessenger),
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, instantMessenger),
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, instantMessenger),
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, email, instantMessenger),
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, email, instantMessenger),
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, email, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, email),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, email),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, email),
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, email, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, email, instantMessenger),
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, email, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, email),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, email),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, email),
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, email, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, email, instantMessenger),
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, email, instantMessenger),
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, email),
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, email),
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, email),
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, instantMessenger),
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, instantMessenger),
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, instantMessenger),
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, email, instantMessenger),
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, email, instantMessenger),
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, email, instantMessenger),
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, email),
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, email),
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, email),
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, instantMessenger),
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, instantMessenger),
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, instantMessenger),
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, email, instantMessenger),
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, email, instantMessenger),
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, email, instantMessenger),
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, email),
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, email),
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, email),
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, instantMessenger),
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, instantMessenger),
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, instantMessenger),
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, email, instantMessenger),
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, email, instantMessenger),
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, email, instantMessenger),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, email),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, email),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, email),
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, instantMessenger),
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, instantMessenger),
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, instantMessenger),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, email, instantMessenger),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, email, instantMessenger),
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, email, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, email),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, email),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, email),
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, email, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, email, instantMessenger),
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, email, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, email),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, email),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, deleteAnyWhere, twinEntity, LOG, email),
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, twinEntity, LOG, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, email, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, email, instantMessenger),
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, twinEntity, LOG, email, instantMessenger)
	;

	/**
	 * Execution moment: {@code _before} or {@code _after} the operation.
	 */
	public final CcpEntityOperationPhase operationPhase;

	/**
	 * Operation type: {@code save}, {@code insert}, {@code update}, {@code delete} or
	 * {@code deleteAnyWhere}.
	 */
	public final CcpEntityDecoratorOperationType operationType;

	/**
	 * Source entity (mainEntity or twinEntity).
	 */
	public final CcpEntityPhase entityPhase;

	/**
	 * Policy for handling a failure while sending the message.
	 */
	public final JnMessageSenderExceptionHandler exceptionHandler;

	private final JnMessageType[] messagesTypes;

	private JnEntitySendMessageToUserWhenWriteOperationType(
			CcpEntityOperationPhase operationPhase,
			CcpEntityDecoratorOperationType operationType,
			CcpEntityPhase entityPhase,
			JnMessageSenderExceptionHandler exceptionHandler,
			JnMessageType... messagesTypes) {
		this.operationPhase = operationPhase;
		this.operationType = operationType;
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

