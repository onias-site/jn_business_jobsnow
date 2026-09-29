package com.jn.entities.decorators.builders;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;

/**
 * Builds {@code JnVersionablePurgeEntity}. Goes along with {@code JnEntityVersionableBuilder} on every
 * versionable entity, always with priority 5 — see the reason for that position in
 * {@code JnVersionablePurgeEntity}. It needs no auxiliary annotation: there is nothing to configure.
 */
public class JnEntityVersionablePurgeBuilder extends CcpCustomDecoratorEntity{

	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		JnVersionablePurgeEntity purge = new JnVersionablePurgeEntity(entity);
		return purge;
	}

}
