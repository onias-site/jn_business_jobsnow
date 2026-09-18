package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Decorator que envia apenas as mensagens do fluxo {@code before} das operações de escrita. Fica na
 * parte externa da cadeia (prioridade alta) para que o envio aconteça antes dos demais decorators e o
 * JSON resultante seja o que segue para dentro. O fluxo {@code after} é responsabilidade de
 * {@code JnSendMessageToUserEntityAfterWrite}.
 */
public class JnSendMessageToUserEntityBeforeWrite extends JnSendMessageToUserEntityOnWrite {

	public JnSendMessageToUserEntityBeforeWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity, annotation);
	}

	public boolean delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.delete);
		boolean deleted = this.entity.delete(_before);
		return deleted;
	}

	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.deleteAnyWhere);
		boolean deleted = this.entity.deleteAnyWhere(_before);
		return deleted;
	}

	public boolean save(CcpJsonRepresentation json) {
		CcpJsonRepresentation _before = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.save);
		boolean inserted = this.entity.save(_before);
		return inserted;
	}
}
