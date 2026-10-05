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
	/** After {@code save} on the main entity, sends an e-mail; a failure throws the error. */
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, email),
	/** After {@code save} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, email),
	/** After {@code save} on the main entity, sends an e-mail; a failure is only logged. */
	afterSaveFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, email),
	/** After {@code save} on the main entity, sends an instant message; a failure throws the error. */
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, instantMessenger),
	/** After {@code save} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, instantMessenger),
	/** After {@code save} on the main entity, sends an instant message; a failure is only logged. */
	afterSaveFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, instantMessenger),
	/** After {@code save} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, save, mainEntity, THROWS, email, instantMessenger),
	/** After {@code save} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, save, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code save} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, save, mainEntity, LOG, email, instantMessenger),
	/** After {@code save} on the twin entity, sends an e-mail; a failure throws the error. */
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, email),
	/** After {@code save} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, email),
	/** After {@code save} on the twin entity, sends an e-mail; a failure is only logged. */
	afterSaveFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, email),
	/** After {@code save} on the twin entity, sends an instant message; a failure throws the error. */
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, instantMessenger),
	/** After {@code save} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, instantMessenger),
	/** After {@code save} on the twin entity, sends an instant message; a failure is only logged. */
	afterSaveFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, instantMessenger),
	/** After {@code save} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, save, twinEntity, THROWS, email, instantMessenger),
	/** After {@code save} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, save, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code save} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, save, twinEntity, LOG, email, instantMessenger),
	/** After {@code insert} on the main entity, sends an e-mail; a failure throws the error. */
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, email),
	/** After {@code insert} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, email),
	/** After {@code insert} on the main entity, sends an e-mail; a failure is only logged. */
	afterInsertFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, email),
	/** After {@code insert} on the main entity, sends an instant message; a failure throws the error. */
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, instantMessenger),
	/** After {@code insert} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, instantMessenger),
	/** After {@code insert} on the main entity, sends an instant message; a failure is only logged. */
	afterInsertFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, instantMessenger),
	/** After {@code insert} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, insert, mainEntity, THROWS, email, instantMessenger),
	/** After {@code insert} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, insert, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code insert} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterInsertFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, insert, mainEntity, LOG, email, instantMessenger),
	/** After {@code insert} on the twin entity, sends an e-mail; a failure throws the error. */
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, email),
	/** After {@code insert} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, email),
	/** After {@code insert} on the twin entity, sends an e-mail; a failure is only logged. */
	afterInsertFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, email),
	/** After {@code insert} on the twin entity, sends an instant message; a failure throws the error. */
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, instantMessenger),
	/** After {@code insert} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, instantMessenger),
	/** After {@code insert} on the twin entity, sends an instant message; a failure is only logged. */
	afterInsertFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, instantMessenger),
	/** After {@code insert} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, insert, twinEntity, THROWS, email, instantMessenger),
	/** After {@code insert} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, insert, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code insert} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterInsertFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, insert, twinEntity, LOG, email, instantMessenger),
	/** After {@code update} on the main entity, sends an e-mail; a failure throws the error. */
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, email),
	/** After {@code update} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, email),
	/** After {@code update} on the main entity, sends an e-mail; a failure is only logged. */
	afterUpdateFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, email),
	/** After {@code update} on the main entity, sends an instant message; a failure throws the error. */
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, instantMessenger),
	/** After {@code update} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, instantMessenger),
	/** After {@code update} on the main entity, sends an instant message; a failure is only logged. */
	afterUpdateFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, instantMessenger),
	/** After {@code update} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, update, mainEntity, THROWS, email, instantMessenger),
	/** After {@code update} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, update, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code update} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterUpdateFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, update, mainEntity, LOG, email, instantMessenger),
	/** After {@code update} on the twin entity, sends an e-mail; a failure throws the error. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, email),
	/** After {@code update} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, email),
	/** After {@code update} on the twin entity, sends an e-mail; a failure is only logged. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, email),
	/** After {@code update} on the twin entity, sends an instant message; a failure throws the error. */
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, instantMessenger),
	/** After {@code update} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, instantMessenger),
	/** After {@code update} on the twin entity, sends an instant message; a failure is only logged. */
	afterUpdateFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, instantMessenger),
	/** After {@code update} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, update, twinEntity, THROWS, email, instantMessenger),
	/** After {@code update} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, update, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code update} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterUpdateFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, update, twinEntity, LOG, email, instantMessenger),
	/** After {@code delete} on the main entity, sends an e-mail; a failure throws the error. */
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, email),
	/** After {@code delete} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, email),
	/** After {@code delete} on the main entity, sends an e-mail; a failure is only logged. */
	afterDeleteFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, email),
	/** After {@code delete} on the main entity, sends an instant message; a failure throws the error. */
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, instantMessenger),
	/** After {@code delete} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, instantMessenger),
	/** After {@code delete} on the main entity, sends an instant message; a failure is only logged. */
	afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, instantMessenger),
	/** After {@code delete} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, delete, mainEntity, THROWS, email, instantMessenger),
	/** After {@code delete} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, delete, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code delete} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, delete, mainEntity, LOG, email, instantMessenger),
	/** After {@code delete} on the twin entity, sends an e-mail; a failure throws the error. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, email),
	/** After {@code delete} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, email),
	/** After {@code delete} on the twin entity, sends an e-mail; a failure is only logged. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, email),
	/** After {@code delete} on the twin entity, sends an instant message; a failure throws the error. */
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, instantMessenger),
	/** After {@code delete} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, instantMessenger),
	/** After {@code delete} on the twin entity, sends an instant message; a failure is only logged. */
	afterDeleteFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, instantMessenger),
	/** After {@code delete} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, delete, twinEntity, THROWS, email, instantMessenger),
	/** After {@code delete} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, delete, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code delete} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, delete, twinEntity, LOG, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure throws the error. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, email),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, email),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure is only logged. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, email),
	/** After {@code deleteAnyWhere} on the main entity, sends an instant message; a failure throws the error. */
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an instant message; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an instant message; a failure is only logged. */
	afterDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, mainEntity, THROWS, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, mainEntity, LENIENT, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	afterDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, mainEntity, LOG, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure throws the error. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, email),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, email),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure is only logged. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, email),
	/** After {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure throws the error. */
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure is only logged. */
	afterDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, deleteAnyWhere, twinEntity, THROWS, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, deleteAnyWhere, twinEntity, LENIENT, email, instantMessenger),
	/** After {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	afterDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, deleteAnyWhere, twinEntity, LOG, email, instantMessenger),
	/** Before {@code save} on the main entity, sends an e-mail; a failure throws the error. */
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, email),
	/** Before {@code save} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, email),
	/** Before {@code save} on the main entity, sends an e-mail; a failure is only logged. */
	beforeSaveFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, email),
	/** Before {@code save} on the main entity, sends an instant message; a failure throws the error. */
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, instantMessenger),
	/** Before {@code save} on the main entity, sends an instant message; a failure is recorded as a warning. */
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, instantMessenger),
	/** Before {@code save} on the main entity, sends an instant message; a failure is only logged. */
	beforeSaveFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, instantMessenger),
	/** Before {@code save} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, save, mainEntity, THROWS, email, instantMessenger),
	/** Before {@code save} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, save, mainEntity, LENIENT, email, instantMessenger),
	/** Before {@code save} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeSaveFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, save, mainEntity, LOG, email, instantMessenger),
	/** Before {@code save} on the twin entity, sends an e-mail; a failure throws the error. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, email),
	/** Before {@code save} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, email),
	/** Before {@code save} on the twin entity, sends an e-mail; a failure is only logged. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, email),
	/** Before {@code save} on the twin entity, sends an instant message; a failure throws the error. */
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, instantMessenger),
	/** Before {@code save} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, instantMessenger),
	/** Before {@code save} on the twin entity, sends an instant message; a failure is only logged. */
	beforeSaveFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, instantMessenger),
	/** Before {@code save} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, save, twinEntity, THROWS, email, instantMessenger),
	/** Before {@code save} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, save, twinEntity, LENIENT, email, instantMessenger),
	/** Before {@code save} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeSaveFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, save, twinEntity, LOG, email, instantMessenger),
	/** Before {@code delete} on the main entity, sends an e-mail; a failure throws the error. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, email),
	/** Before {@code delete} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, email),
	/** Before {@code delete} on the main entity, sends an e-mail; a failure is only logged. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, email),
	/** Before {@code delete} on the main entity, sends an instant message; a failure throws the error. */
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, instantMessenger),
	/** Before {@code delete} on the main entity, sends an instant message; a failure is recorded as a warning. */
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, instantMessenger),
	/** Before {@code delete} on the main entity, sends an instant message; a failure is only logged. */
	beforeDeleteFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, instantMessenger),
	/** Before {@code delete} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, delete, mainEntity, THROWS, email, instantMessenger),
	/** Before {@code delete} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, delete, mainEntity, LENIENT, email, instantMessenger),
	/** Before {@code delete} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeDeleteFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, delete, mainEntity, LOG, email, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an e-mail; a failure throws the error. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, email),
	/** Before {@code delete} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, email),
	/** Before {@code delete} on the twin entity, sends an e-mail; a failure is only logged. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, email),
	/** Before {@code delete} on the twin entity, sends an instant message; a failure throws the error. */
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an instant message; a failure is only logged. */
	beforeDeleteFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, delete, twinEntity, THROWS, email, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, delete, twinEntity, LENIENT, email, instantMessenger),
	/** Before {@code delete} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeDeleteFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, delete, twinEntity, LOG, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure throws the error. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, email),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, email),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail; a failure is only logged. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, email),
	/** Before {@code deleteAnyWhere} on the main entity, sends an instant message; a failure throws the error. */
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an instant message; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an instant message; a failure is only logged. */
	beforeDeleteAnyWhereFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, mainEntity, THROWS, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, mainEntity, LENIENT, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the main entity, sends an e-mail and an instant message; a failure is only logged. */
	beforeDeleteAnyWhereFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, mainEntity, LOG, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure throws the error. */
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, email),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, email),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail; a failure is only logged. */
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, deleteAnyWhere, twinEntity, LOG, email),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure throws the error. */
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an instant message; a failure is only logged. */
	beforeDeleteAnyWhereFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, deleteAnyWhere, twinEntity, LOG, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure throws the error. */
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, deleteAnyWhere, twinEntity, THROWS, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure is recorded as a warning. */
	beforeDeleteAnyWhereFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, deleteAnyWhere, twinEntity, LENIENT, email, instantMessenger),
	/** Before {@code deleteAnyWhere} on the twin entity, sends an e-mail and an instant message; a failure is only logged. */
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

	/** The kinds of message sent (e-mail, instant message or both). */
	private final JnMessageType[] messagesTypes;

	/**
	 * Associates the item with the values its name expresses.
	 * @param operationPhase when the message is sent
	 * @param operationType the write operation
	 * @param entityPhase the source entity
	 * @param exceptionHandler what happens when the sending fails
	 * @param messagesTypes the kinds of message sent
	 */
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

