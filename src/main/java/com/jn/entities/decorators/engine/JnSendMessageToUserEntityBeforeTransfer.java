package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Decorator that sends only the messages of the {@code before} flow of data transfers. It stays in
 * the outer part of the chain (high priority) so that the sending happens before the other decorators
 * and the resulting JSON is what goes further in. The {@code after} flow is the responsibility of
 * {@code JnSendMessageToUserEntityAfterTransfer}.
 */
public class JnSendMessageToUserEntityBeforeTransfer extends JnSendMessageToUserEntityOnTransfer {

	public JnSendMessageToUserEntityBeforeTransfer(CcpEntity entity, JnEntitySendMessageToUserWhenTransfer annotation) {
		super(entity, annotation);
	}

	public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation preparedJson = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.copyDataTo, targetEntity);
		boolean copied = this.entity.copyDataTo(preparedJson, targetEntity);
		return copied;
	}

	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation preparedJson = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.transferDataTo, targetEntity);
		boolean transfered = this.entity.transferDataTo(preparedJson, targetEntity);
		return transfered;
	}
}
