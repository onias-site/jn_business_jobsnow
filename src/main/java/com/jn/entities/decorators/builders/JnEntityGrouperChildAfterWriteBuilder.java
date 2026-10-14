package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntityGrouperChild;

/**
 * Builds the decorator that puts the record in its groups after the writes. Declare it in
 * {@code @CcpEntityCustomDecorators} with priority 5, inside the field transformers, so that it sees the record as
 * written.
 */
public class JnEntityGrouperChildAfterWriteBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the {@code afterDecoratorClass} of {@code @JnEntityGrouperChild}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityGrouperChild.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.afterDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class, JnEntityGrouperChild.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity, annotation);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
