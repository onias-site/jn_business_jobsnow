package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntityVersionable;

/** Builds the versioning decorator named by {@code @JnEntityVersionable}. */
public class JnEntityVersionableBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the decorator class of {@code @JnEntityVersionable}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
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
