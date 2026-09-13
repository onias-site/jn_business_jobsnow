package com.jn.entities.decorators.builders;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.jn.entities.decorators.engine.JnAsyncWriterEntity;

public class JnEntityAsyncWriterBuilder implements CcpEntityBuilder{

	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var ent = new JnAsyncWriterEntity(entity);
		return ent;
	}

}
