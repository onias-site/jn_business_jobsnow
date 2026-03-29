package com.jn.entities.decorators2;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity2;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityAsyncWriter;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpDecoratorEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpEntityDelegator;
import com.jn.mensageria.JnFunctionMensageriaSender;

public class JnAsyncWriterEntity extends CcpEntityDelegator implements CcpDecoratorEntity<CcpEntityAsyncWriter> {

	final Class<?> clazz;

	public JnAsyncWriterEntity(CcpEntity2 entity, Class<?> clazz) {
		super(entity, 5);
		this.clazz = clazz;
	}

	public boolean isThisEntityDecorated(Class<CcpEntityAsyncWriter> annotation) {
		boolean annotationPresent = this.clazz.isAnnotationPresent(annotation);
		return annotationPresent;
	}

	public CcpEntityAsyncWriter getAnnotation() {
		CcpEntityAsyncWriter annotation = this.clazz.getAnnotation(CcpEntityAsyncWriter.class);
		return annotation;
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

	private CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation) {
	
//		FIXME JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, operation);
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(null, operation);
		CcpJsonRepresentation apply = sender.apply(json);
		return apply;
	}

}
