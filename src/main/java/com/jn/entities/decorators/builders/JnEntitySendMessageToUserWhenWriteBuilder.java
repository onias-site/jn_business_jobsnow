package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;

public class JnEntitySendMessageToUserWhenWriteBuilder implements CcpEntityBuilder{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntitySendMessageToUserWhenWrite.class);
		Class<CcpEntity> value = (Class<CcpEntity>) annotation.decoratorClass();

		try {
			Constructor<CcpEntity> constructor = value.getConstructor(CcpEntity.class, JnEntitySendMessageToUserWhenWrite.class);
			CcpEntity newInstance = constructor.newInstance(entity, annotation);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
