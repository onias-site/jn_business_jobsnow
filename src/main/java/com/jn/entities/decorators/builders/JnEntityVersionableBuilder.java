package com.jn.entities.decorators.builders;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.jn.entities.decorators.engine.JnVersionableEntity;

public class JnEntityVersionableBuilder implements CcpEntityBuilder{

	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var ent = new JnVersionableEntity(entity);
		return ent;
	}

}
