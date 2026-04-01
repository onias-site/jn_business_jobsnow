package com.jn.entities.decorators;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.jn.mensageria.JnFunctionMensageriaSender;

public class JnAsyncWriterEntity extends CcpEntityDelegator  {

	public JnAsyncWriterEntity(CcpEntity entity, Class<?> clazz) {
		super(entity);
	}

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.delete);
		return apply;
	}

	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.deleteAnyWhere);
		return apply;
	}

	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.save);
		return apply;
	}

	public CcpJsonRepresentation transferDataTo(CcpJsonRepresentation json, CcpEntity... entities) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.transferDataTo);
		return apply;
	}

	public CcpJsonRepresentation copyDataTo(CcpJsonRepresentation json, CcpEntity... entities) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.copyDataTo);
		return apply;
	}
	
	private CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation) {
	
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, operation);
		CcpJsonRepresentation apply = sender.apply(json);
		return apply;
	}

}
