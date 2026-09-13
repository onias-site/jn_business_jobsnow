package com.jn.entities.decorators.builders;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityBuilder;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUser;
import com.jn.entities.decorators.engine.JnSendMessageToUserEntity;

public class JnEntitySendMessageToUserBuilder implements CcpEntityBuilder{

	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		JnEntitySendMessageToUser annotation = configurationClass.getAnnotation(JnEntitySendMessageToUser.class);
		JnSendMessageToUserEntity ent = new JnSendMessageToUserEntity(entity, annotation);
		return ent;
	}

}
