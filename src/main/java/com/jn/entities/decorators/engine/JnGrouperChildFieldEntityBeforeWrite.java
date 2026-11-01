package com.jn.entities.decorators.engine;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;

/**
 * The {@code beforeDecoratorClass} of {@code @CcpEntityGrouperChild} that goes with
 * {@code JnGrouperChildFieldEntityAfterWrite}: it only delegates. A value shared by several records never leaves its
 * groups, so nothing has to be read before the write.
 */
public class JnGrouperChildFieldEntityBeforeWrite extends CcpEntityDelegator {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildFieldEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity);
	}
}
