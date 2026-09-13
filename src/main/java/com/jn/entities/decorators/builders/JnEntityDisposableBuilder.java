package com.jn.entities.decorators.builders;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.decorators.annotations.JnEntityDisposable;
import com.jn.entities.decorators.engine.JnDisposableEntity;

public class JnEntityDisposableBuilder implements CcpEntityBuilder{

	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		JnEntityDisposable annotation = configurationClass.getAnnotation(JnEntityDisposable.class);
		CcpEntityExpurgableOptions timeOption = annotation.timeOption();
		JnDisposableEntity jnDisposableEntity = new JnDisposableEntity(entity, timeOption);
		return jnDisposableEntity;
	}

}
