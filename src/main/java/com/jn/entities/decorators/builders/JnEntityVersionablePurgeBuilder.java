package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntityVersionablePurge;

/**
 * Builds the decorator named by {@code @JnEntityVersionablePurge} (usually {@code JnVersionablePurgeEntity}). Goes along
 * with {@code JnEntityVersionableBuilder} on every versionable entity, always with priority 5 — see the reason for that
 * position in {@code JnVersionablePurgeEntity}.
 */
public class JnEntityVersionablePurgeBuilder extends CcpCustomDecoratorEntity{

	/**
	 * Wraps the entity with the {@code value} of {@code @JnEntityVersionablePurge}.
	 * @param configurationClass the configurator class
	 * @param entity the entity decorated so far
	 * @return the decorated entity
	 */
	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityVersionablePurge.class);
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
