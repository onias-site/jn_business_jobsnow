package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Decorator that sends only the messages of the {@code after} flow of data transfers. It stays in the
 * inner part of the chain (low priority) and only fires when the transfer actually happened, that is,
 * when there was a source record to be copied or moved. The {@code before} flow is the
 * responsibility of {@code JnSendMessageToUserEntityBeforeTransfer}.
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
