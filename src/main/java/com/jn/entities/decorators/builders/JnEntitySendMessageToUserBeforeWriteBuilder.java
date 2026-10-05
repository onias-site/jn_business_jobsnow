package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Builds the decorator that sends the messages of the {@code before} flow of write operations.
 * Declare it in {@code @CcpEntityCustomDecorators} with a high priority, so that it stays in the
 * outer part of the chain.
 */
public class JnEntitySendMessageToUserBeforeWriteBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the {@code beforeDecoratorClass} of {@code @JnEntitySendMessageToUserWhenWrite}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntitySendMessageToUserWhenWrite.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.beforeDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class, JnEntitySendMessageToUserWhenWrite.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity, annotation);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
