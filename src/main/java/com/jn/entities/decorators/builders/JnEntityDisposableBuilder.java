package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.decorators.annotations.JnEntityDisposable;

public class JnEntityDisposableBuilder extends CcpCustomDecoratorEntity{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntityDisposable.class);
		Class<CcpEntity> value = (Class<CcpEntity>) annotation.value();
		CcpEntityExpurgableOptions timeOption = annotation.timeOption();

		try {
			Constructor<CcpEntity> constructor = value.getConstructor(CcpEntity.class, CcpEntityExpurgableOptions.class);
			CcpEntity newInstance = constructor.newInstance(entity, timeOption);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
