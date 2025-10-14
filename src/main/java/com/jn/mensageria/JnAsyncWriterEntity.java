package com.jn.mensageria;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityOperationType;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityDecoratorFactory;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityDelegator;

public class JnAsyncWriterEntity extends CcpEntityDelegator implements CcpEntityDecoratorFactory   {

	public JnAsyncWriterEntity(CcpEntity entity) {
		super(entity);
	}

	public JnAsyncWriterEntity() {
		super(null);
	}

	public CcpEntity getEntity(CcpEntity entity) {
		return new JnAsyncWriterEntity(entity);
	}
	
	public CcpJsonRepresentation save(CcpJsonRepresentation json, String id) {
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, CcpEntityOperationType.save);
		CcpJsonRepresentation apply = sender.apply(json);
		return apply;
	}
	
	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, CcpEntityOperationType.delete);
		CcpJsonRepresentation apply = sender.apply(json);
		return apply;
	}
	
	public CcpJsonRepresentation transferToReverseEntity(CcpJsonRepresentation json) {
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, CcpEntityOperationType.transferToReverseEntity);
		CcpJsonRepresentation apply = sender.apply(json);
		return apply;
	}

}
