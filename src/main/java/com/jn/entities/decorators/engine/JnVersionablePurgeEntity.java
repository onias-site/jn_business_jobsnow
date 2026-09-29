package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;

/**
 * Enqueues the history purge when a versionable entity record is deleted from everywhere
 * ({@code deleteAnyWhere}).
 *
 * <p>It is a decorator separate from {@code JnVersionableEntity} because of its position in the chain. The
 * versionable one must be the innermost (priority 2): it reimplements bulk writing, and outside the
 * twin it would skip the twin's overrides. But the twin (priority 4) resolves
 * {@code deleteAnyWhere} on its own and does not pass it inward — so, in entities that are both twin and
 * versionable, the purge was never called. This decorator sits at 5, outside the twin and inside
 * the field transformer (6) and the asynchronous writer (8): it receives the already transformed json and
 * runs in the queue consumer. It does not take over the operation: it delegates the deletion inward and only
 * adds the purge.
 *
 * <p>The purge is asynchronous because the history grows one row per operation ever performed on the
 * record; deleting it inline would penalize whoever only asked to remove a document. The return value is
 * that of the real deletion: the record existed before the removal.
 */
public class JnVersionablePurgeEntity extends CcpEntityDelegator {

	public JnVersionablePurgeEntity(CcpEntity entity) {
		super(entity);
	}

	public boolean deleteAnyWhere(CcpJsonRepresentation json) {

		CcpJsonRepresentation deletionRequest = JnVersionableEntity.getDeletionRequest(this.entity, json);

		boolean existedBeforeTheDeletion = this.entity.deleteAnyWhere(json);

		JnBusinessDeleteVersionableRecords.INSTANCE.sendToMensageria(deletionRequest);

		return existedBeforeTheDeletion;
	}
}
