package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntityVersionable;

public class JnEntityVersionableBuilder extends CcpCustomDecoratorEntity{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityVersionable.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.value();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
