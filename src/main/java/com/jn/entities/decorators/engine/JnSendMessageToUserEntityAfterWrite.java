package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Decorator that sends only the messages of the {@code after} flow of write operations. It stays in the
 * inner part of the chain (low priority). On {@code delete} it only fires when the record was found and
 * removed; on {@code save} it always fires, and the outcome ({@code insert} when {@code save} returns
 * {@code true}, {@code update} when it returns {@code false}) decides which configured items send a
 * message. The {@code before} flow is the responsibility of
 * {@code JnSendMessageToUserEntityBeforeWrite}.
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

		CcpEntityDecoratorOperationType outcome = CcpEntityDecoratorOperationType.save.getOutcome(inserted);
		this.executeFlow(json, CcpEntityOperationPhase._after, outcome);
		return inserted;
	}
}
