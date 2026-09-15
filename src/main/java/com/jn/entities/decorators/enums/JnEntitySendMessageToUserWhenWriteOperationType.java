package com.jn.entities.decorators.enums;

import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.delete;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.deleteAnyWhere;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.save;
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
 * Prevê todas as combinações possíveis dos campos {@code operationPhase}, {@code operationType},
 * {@code entityPhase}, {@code messagesTypes} e {@code exceptionHandler} de
 * {@code @JnEntitySendMessageToUserWhenWriteOperation}. Cada item encapsula os valores que o seu
 * nome expressa, de modo que a anotação declare uma única constante em vez dos cinco campos.
 *
 * <p>O nome de cada item é lido como uma frase: {@code [operationPhase][operationType]From
 * [entityPhase]Send[messagesTypes]AndIfFails[exceptionHandler]}.
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
	 * Momento de execução: {@code _before} (antes) ou {@code _after} (depois) da operação.
	 */
	public final CcpEntityOperationPhase operationPhase;

	/**
	 * Tipo da operação: {@code save}, {@code delete} ou {@code deleteAnyWhere}.
	 */
	public final CcpEntityDecoratorOperationType operationType;

	/**
	 * Entidade de origem (mainEntity ou twinEntity).
	 */
	public final CcpEntityPhase entityPhase;

	/**
	 * Política de tratamento de falha no envio da mensagem.
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
	 * Tipos de mensagem a enviar. Nunca vem vazio: toda combinação prevista tem ao menos um tipo.
	 */
	public JnMessageType[] messagesTypes() {
		JnMessageType[] copy = this.messagesTypes.clone();
		return copy;
	}
}

