package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Decorator que envia apenas as mensagens do fluxo {@code after} das transferências de dados. Fica na
 * parte interna da cadeia (prioridade baixa) e só dispara quando a transferência aconteceu de fato,
 * ou seja, quando havia registro de origem para ser copiado ou movido. O fluxo {@code before} é
 * responsabilidade de {@code JnSendMessageToUserEntityBeforeTransfer}.
 */
public class JnSendMessageToUserEntityAfterTransfer extends JnSendMessageToUserEntityOnTransfer {

	public JnSendMessageToUserEntityAfterTransfer(CcpEntity entity, JnEntitySendMessageToUserWhenTransfer annotation) {
		super(entity, annotation);
	}

	public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		boolean copied = this.entity.copyDataTo(json, targetEntity);

		boolean nothingWasCopied = false == copied;

		if(nothingWasCopied) {
			return false;
		}

		this.executeFlow(json, CcpEntityOperationPhase._after, CcpEntityDecoratorTransferType.copyDataTo, targetEntity);
		return copied;
	}

	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		boolean transfered = this.entity.transferDataTo(json, targetEntity);

		boolean nothingWasTransfered = false == transfered;

		if(nothingWasTransfered) {
			return false;
		}

		this.executeFlow(json, CcpEntityOperationPhase._after, CcpEntityDecoratorTransferType.transferDataTo, targetEntity);
		return transfered;
	}
}
