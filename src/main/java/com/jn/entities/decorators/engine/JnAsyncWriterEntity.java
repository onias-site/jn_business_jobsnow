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

	public JnAsyncWriterEntity(CcpEntity entity) {
		super(entity);
	}

	public boolean delete(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.delete);
		return sent;
	}

	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.deleteAnyWhere);
		return sent;
	}

	public boolean save(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.save);
		return sent;
	}

	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		CcpJsonRepresentation jsonWithTargetEntity = CcpEntityOperationType.putEntityToTransfer(json, targetEntity);
		boolean sent = this.sendToMensageria(jsonWithTargetEntity, CcpEntityOperationType.transferDataTo);
		return sent;
	}

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
