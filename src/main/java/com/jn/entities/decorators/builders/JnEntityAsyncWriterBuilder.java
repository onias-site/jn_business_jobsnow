package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.jn.entities.decorators.annotations.JnEntityAsyncWriter;

public class JnEntityAsyncWriterBuilder implements CcpEntityBuilder{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityAsyncWriter.class);
		Class<CcpEntity> value = (Class<CcpEntity>) annotation.value();

		try {
			Constructor<CcpEntity> constructor = value.getConstructor(CcpEntity.class);
			CcpEntity newInstance = constructor.newInstance(entity);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
