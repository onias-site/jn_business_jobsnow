package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

/**
 * Builds the decorator that sends the messages of the {@code after} flow of write operations.
 * Declare it in {@code @CcpEntityCustomDecorators} with a low priority, so that it stays in the
 * inner part of the chain and only fires after the write.
 */
public class JnEntitySendMessageToUserAfterWriteBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the {@code afterDecoratorClass} of {@code @JnEntitySendMessageToUserWhenWrite}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntitySendMessageToUserWhenWrite.class);
		Class<CcpEntity> decoratorClass = (Class<CcpEntity>) annotation.afterDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = decoratorClass.getConstructor(CcpEntity.class, JnEntitySendMessageToUserWhenWrite.class);
			CcpEntity decoratedEntity = constructor.newInstance(entity, annotation);
			return decoratedEntity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
