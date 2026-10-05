package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationPhase;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Decorator that sends only the messages of the {@code before} flow of write operations. It stays in
 * the outer part of the chain (high priority) so that the sending happens before the other decorators
 * and the resulting JSON is what goes further in. The {@code after} flow is the responsibility of
 * {@code JnSendMessageToUserEntityAfterWrite}.
 */
public class JnSendMessageToUserEntityBeforeWrite extends JnSendMessageToUserEntityOnWrite {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the messages configured for the entity
	 */
	public JnSendMessageToUserEntityBeforeWrite(CcpEntity entity, JnEntitySendMessageToUserWhenWrite annotation) {
		super(entity, annotation);
	}

	/**
	 * Sends the {@code before delete} messages and deletes with the resulting JSON.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation preparedJson = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.delete);
		boolean deleted = this.entity.delete(preparedJson);
		return deleted;
	}

	/**
	 * Sends the {@code before deleteAnyWhere} messages and deletes everywhere with the resulting JSON.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation preparedJson = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.deleteAnyWhere);
		boolean deleted = this.entity.deleteAnyWhere(preparedJson);
		return deleted;
	}

	/**
	 * Sends the {@code before save} messages and saves the resulting JSON.
	 * @param json the record
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		CcpJsonRepresentation preparedJson = this.executeFlow(json, CcpEntityOperationPhase._before, CcpEntityDecoratorOperationType.save);
		boolean inserted = this.entity.save(preparedJson);
		return inserted;
	}
}
