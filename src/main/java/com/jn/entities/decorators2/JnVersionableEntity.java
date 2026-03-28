package com.jn.entities.decorators2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccp.business.CcpBusiness;
import com.ccp.especifications.db.utils.entity.CcpEntity2;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpDecoratorEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpEntityDelegator;

public class JnVersionableEntity extends CcpEntityDelegator implements CcpDecoratorEntity<CcpEntityVersionable>{
	
	final Class<?>  clazz;
	
	public JnVersionableEntity(CcpEntity2 entity, Class<?> clazz) {
		super(entity);
		this.clazz = clazz;
	}
	
	public boolean isThisEntityDecorated(Class<CcpEntityVersionable> annotation) {
		boolean annotationPresent = this.clazz.isAnnotationPresent(annotation);
		return annotationPresent;
	}

	public List<CcpBusiness> getFlow() {
		return new ArrayList<>();
	}

	public Map<Class<?>, List<CcpBusiness>> getExceptionHandlers() {
		Map<Class<?>, List<CcpBusiness>> result = new HashMap<>();
		return result;
	}

	public CcpEntityVersionable getAnnotation() {
		CcpEntityVersionable annotation = this.clazz.getAnnotation(CcpEntityVersionable.class);
		return annotation;
	}
	
	
	
}
