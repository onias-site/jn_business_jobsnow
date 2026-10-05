package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.jn.mensageria.JnFunctionMensageriaSender;

/**
 * Decorator that turns synchronous entity operations into asynchronous operations via messaging.
 * Any call to {@code save}, {@code delete}, {@code deleteAnyWhere}, {@code transferDataTo}
 * or {@code copyDataTo} is intercepted and sent to PubSub via {@code JnFunctionMensageriaSender}.
 */
public class JnAsyncWriterEntity extends CcpEntityDelegator  {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far (what the consumer will run)
	 */
	public JnAsyncWriterEntity(CcpEntity entity) {
		super(entity);
	}

	/**
	 * Publishes the delete to messaging.
	 * @param json the record
	 * @return whether the message was accepted (not whether the record existed)
	 */
	public boolean delete(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.delete);
		return sent;
	}

	/**
	 * Publishes the deleteAnyWhere to messaging.
	 * @param json the record
	 * @return whether the message was accepted
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.deleteAnyWhere);
		return sent;
	}

	/**
	 * Publishes the save to messaging.
	 * @param json the record
	 * @return whether the message was accepted (not whether it inserted)
	 */
	public boolean save(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.save);
		return sent;
	}

	/**
	 * Publishes the transfer to messaging, with the target entity in the message.
	 * @param json the record
	 * @param targetEntity the target entity
	 * @return whether the message was accepted
	 */
	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation jsonWithTargetEntity = CcpEntityOperationType.putEntityToTransfer(json, targetEntity);
		boolean sent = this.sendToMensageria(jsonWithTargetEntity, CcpEntityOperationType.transferDataTo);
		return sent;
	}

	/**
	 * Publishes the copy to messaging, with the target entity in the message.
	 * @param json the record
	 * @param targetEntity the target entity
	 * @return whether the message was accepted
	 */
	public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation jsonWithTargetEntity = CcpEntityOperationType.putEntityToTransfer(json, targetEntity);
		boolean sent = this.sendToMensageria(jsonWithTargetEntity, CcpEntityOperationType.copyDataTo);
		return sent;
	}

	/**
	 * Sends the operation to the messaging system. Since execution is asynchronous, at call time there is
	 * still no way to know whether the document will be inserted, updated or removed, so the return value
	 * only indicates that the message was accepted by the topic. This is the only point in the hierarchy
	 * where the boolean does not carry the meaning defined in {@code CcpEntity}.
	 */
	private boolean sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation) {
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, operation);
		CcpJsonRepresentation response = sender.execute(json);
		boolean emptyResponse = response.isEmpty();
		boolean sent = false == emptyResponse;
		return sent;
	}
}
