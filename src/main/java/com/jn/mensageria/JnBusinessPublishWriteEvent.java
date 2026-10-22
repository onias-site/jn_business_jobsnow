package com.jn.mensageria;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Announces a write of an entity to whoever listens to it in {@link JnEntityWriteEventSubscribers}: each subscriber is
 * published to the messaging (the topic is its class name) with the written record, so it runs asynchronously, in the
 * consumer, after the write. An event nobody listens to publishes nothing.
 * <p>
 * The entity that writes declares a subclass with a no-arg constructor naming itself and the write, and puts it in the
 * {@code execute} of its side effect annotation (for example {@code @CcpAfterSaveFromMainEntity}). The subscribers are
 * instantiated by class name, so the process that writes and the consumer of the messaging must have the cost center of
 * the subscribers on their classpath (a dependency of the application module, never an import of the business module).
 */
public abstract class JnBusinessPublishWriteEvent implements CcpBusiness {

	/** The entity that is written. */
	private final CcpEntity entity;

	/** The write announced. */
	private final CcpEntityDecoratorOperationType writeOperation;

	/**
	 * Names the event.
	 * @param entity the entity that is written
	 * @param writeOperation the write announced
	 */
	protected JnBusinessPublishWriteEvent(CcpEntity entity, CcpEntityDecoratorOperationType writeOperation) {
		this.entity = entity;
		this.writeOperation = writeOperation;
	}

	/**
	 * Publishes the written record to each subscriber of the event; the subscribers come in a single read by id.
	 * @param json the written record
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation eventKey = JnEntityWriteEventSubscribers.getEventKey(this.entity, this.writeOperation);

		JnBusinessPublishToTheWriteEventSubscribers publishToTheSubscribers = new JnBusinessPublishToTheWriteEventSubscribers(json);

		new CcpGetEntityId(eventKey)
		.toBeginProcedureAnd()
			.ifThisIdIsPresentInEntity(JnEntityWriteEventSubscribers.ENTITY).executeAction(publishToTheSubscribers)
			.andFinallyReturningTheseFields(JsonFieldNames.inexistentField)
		.endThisProcedure(this, CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
		;
		return json;
	}

	/** Fields of the procedure. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code inexistentField} field: the procedure returns no field. */
		inexistentField
	}
}
