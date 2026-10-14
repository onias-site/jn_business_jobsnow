package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntityGrouperChild;

/**
 * Builds the decorator that takes the record out of its groups before the writes. Declare it in
 * {@code @CcpEntityCustomDecorators} with priority 7, so that it reads the stored record before it changes.
 */
public class JnEntityGrouperChildBeforeWriteBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the {@code beforeDecoratorClass} of {@code @JnEntityGrouperChild}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityGrouperChild.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.beforeDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class, JnEntityGrouperChild.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity, annotation);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
