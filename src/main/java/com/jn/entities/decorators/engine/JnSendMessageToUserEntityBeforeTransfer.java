package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorTransferType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Decorator que envia apenas as mensagens do fluxo {@code before} das transferências de dados. Fica
 * na parte externa da cadeia (prioridade alta) para que o envio aconteça antes dos demais decorators
 * e o JSON resultante seja o que segue para dentro. O fluxo {@code after} é responsabilidade de
 * {@code JnSendMessageToUserEntityAfterTransfer}.
 */
public class JnSendMessageToUserEntityBeforeTransfer extends JnSendMessageToUserEntityOnTransfer {

	public JnSendMessageToUserEntityBeforeTransfer(CcpEntity entity, JnEntitySendMessageToUserWhenTransfer annotation) {
		super(entity, annotation);
	}

	public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.copyDataTo, targetEntity);
		boolean copied = this.entity.copyDataTo(_before, targetEntity);
		return copied;
	}

	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorTransferType.transferDataTo, targetEntity);
		boolean transfered = this.entity.transferDataTo(_before, targetEntity);
		return transfered;
	}
}
