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
 * Prevê todas as combinações possíveis dos campos {@code operationPhase}, {@code transferType},
 * {@code entityPhase}, {@code messagesTypes} e {@code exceptionHandler} de
 * {@code @JnEntitySendMessageToUserWhenTransferOperation}. Cada item encapsula os valores que o seu
 * nome expressa, de modo que a anotação declare uma única constante em vez dos cinco campos.
 *
 * <p>O nome de cada item é lido como uma frase: {@code [operationPhase][transferType]From
 * [entityPhase]Send[messagesTypes]AndIfFails[exceptionHandler]}.
 */
public enum JnEntitySendMessageToUserWhenTransferOperationType {
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, email),
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, email),
	afterTransferDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, email),
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, instantMessenger),
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, instantMessenger),
	afterTransferDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, instantMessenger),
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, mainEntity, THROWS, email, instantMessenger),
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, mainEntity, LENIENT, email, instantMessenger),
	afterTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, transferDataTo, mainEntity, LOG, email, instantMessenger),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, email),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, email),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, email),
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, instantMessenger),
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, instantMessenger),
	afterTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, instantMessenger),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, transferDataTo, twinEntity, THROWS, email, instantMessenger),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, transferDataTo, twinEntity, LENIENT, email, instantMessenger),
	afterTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, transferDataTo, twinEntity, LOG, email, instantMessenger),
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, email),
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, email),
	afterCopyDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, email),
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, instantMessenger),
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, instantMessenger),
	afterCopyDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, instantMessenger),
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, mainEntity, THROWS, email, instantMessenger),
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, mainEntity, LENIENT, email, instantMessenger),
	afterCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, copyDataTo, mainEntity, LOG, email, instantMessenger),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, email),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, email),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, email),
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, instantMessenger),
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, instantMessenger),
	afterCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, instantMessenger),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_after, copyDataTo, twinEntity, THROWS, email, instantMessenger),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_after, copyDataTo, twinEntity, LENIENT, email, instantMessenger),
	afterCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_after, copyDataTo, twinEntity, LOG, email, instantMessenger),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, email),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, email),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, email),
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, instantMessenger),
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, instantMessenger),
	beforeTransferDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, instantMessenger),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, mainEntity, THROWS, email, instantMessenger),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, mainEntity, LENIENT, email, instantMessenger),
	beforeTransferDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, transferDataTo, mainEntity, LOG, email, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, email),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, email),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, email),
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, transferDataTo, twinEntity, THROWS, email, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, transferDataTo, twinEntity, LENIENT, email, instantMessenger),
	beforeTransferDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, transferDataTo, twinEntity, LOG, email, instantMessenger),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, email),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, email),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, email),
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, instantMessenger),
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, instantMessenger),
	beforeCopyDataFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, instantMessenger),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, mainEntity, THROWS, email, instantMessenger),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, mainEntity, LENIENT, email, instantMessenger),
	beforeCopyDataFromMainEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, copyDataTo, mainEntity, LOG, email, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, email),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, email),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, email),
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnInstantMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsThrowAnError(_before, copyDataTo, twinEntity, THROWS, email, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsSaveAWarning(_before, copyDataTo, twinEntity, LENIENT, email, instantMessenger),
	beforeCopyDataFromTwinEntitySendAnEmailMessageAndInstantMessageAndIfFailsLogTheError(_before, copyDataTo, twinEntity, LOG, email, instantMessenger)
	;

	/**
	 * Momento de execução: {@code _before} (antes) ou {@code _after} (depois) da transferência.
	 */
	public final CcpEntityOperationPhase operationPhase;

	/**
	 * Tipo da transferência: {@code copyDataTo} ou {@code transferDataTo}.
	 */
	public final CcpEntityDecoratorTransferType transferType;

	/**
	 * Entidade de origem (mainEntity ou twinEntity).
	 */
	public final CcpEntityPhase entityPhase;

	/**
	 * Política de tratamento de falha no envio da mensagem.
	 */
	public final JnMessageSenderExceptionHandler exceptionHandler;

	private final JnMessageType[] messagesTypes;

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
	 * Tipos de mensagem a enviar. Nunca vem vazio: toda combinação prevista tem ao menos um tipo.
	 */
	public JnMessageType[] messagesTypes() {
		JnMessageType[] copy = this.messagesTypes.clone();
		return copy;
	}
}

