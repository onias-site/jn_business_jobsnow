package com.jn.entities.decorators.engine;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;

/**
 * The {@code afterDecoratorClass} of {@code @CcpEntityGrouperChild} that goes with
 * {@code JnGrouperChildAsJsonEntityBeforeWrite}: it only delegates. The child kept as a JSON text holds the values as they
 * came, which only the outer part of the chain (before the field transformers) still has, so the whole work is done
 * there.
 */
public class JnGrouperChildAsJsonEntityAfterWrite extends CcpEntityDelegator {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildAsJsonEntityAfterWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity);
	}
}
