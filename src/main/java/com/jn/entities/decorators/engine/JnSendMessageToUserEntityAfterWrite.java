package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Decorator que envia apenas as mensagens do fluxo {@code after} das operações de escrita. Fica na
 * parte interna da cadeia (prioridade baixa) e só dispara quando a operação aconteceu de fato: o
 * {@code delete} encontrou o registro para remover e o {@code save} incluiu um documento novo. O
 * fluxo {@code before} é responsabilidade de {@code JnSendMessageToUserEntityBeforeWrite}.
 */
public class JnSendMessageToUserEntityAfterWrite extends JnSendMessageToUserEntityOnWrite {

	public JnSendMessageToUserEntityAfterWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity, annotation);
	}

	public boolean delete(CcpJsonRepresentation json) {
		boolean deleted = this.entity.delete(json);

		boolean nothingWasDeleted = false == deleted;

		if(nothingWasDeleted) {
			return false;
		}

		this.executeFlow(json, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.delete);
		return deleted;
	}

	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean deleted = this.entity.deleteAnyWhere(json);

		boolean nothingWasDeleted = false == deleted;

		if(nothingWasDeleted) {
			return false;
		}

		this.executeFlow(json, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.deleteAnyWhere);
		return deleted;
	}

	public boolean save(CcpJsonRepresentation json) {
		boolean inserted = this.entity.save(json);

		boolean documentWasOnlyUpdated = false == inserted;

		if(documentWasOnlyUpdated) {
			return false;
		}

		this.executeFlow(json, CcpEntityOperationPhase._after, CcpEntityDecoratorOperationType.save);
		return inserted;
	}
}
