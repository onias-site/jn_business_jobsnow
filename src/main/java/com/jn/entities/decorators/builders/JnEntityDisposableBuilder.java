package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.decorators.annotations.JnEntityDisposable;

/** Builds the expiration decorator named by {@code @JnEntityDisposable}. */
public class JnEntityDisposableBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the decorator class of {@code @JnEntityDisposable}, giving it the granularity.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityDisposable.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.value();
		CcpEntityExpurgableOptions timeOption = annotation.timeOption();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class, CcpEntityExpurgableOptions.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity, timeOption);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
