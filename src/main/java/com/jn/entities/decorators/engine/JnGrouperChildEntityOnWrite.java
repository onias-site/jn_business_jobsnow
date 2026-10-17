package com.jn.entities.decorators.engine;

import java.util.List;
import java.util.Map;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.jn.utils.JnGroupers;

/**
 * Base shared by the decorators that keep the records of an entity ({@code @CcpEntityGrouperChild}) in the groups of
 * {@code JnEntityGrouper}. Each side of the entity has its side of the grouper: the main records are children of the main
 * groups, the twin records of the twin groups. The child is the id of the record, the same on both sides, and the child
 * name is the name of the main entity, so a group has the same key on both sides.
 */
public abstract class JnGrouperChildEntityOnWrite extends CcpEntityDelegator {

	/** The grouping configured for the entity. */
	private final CcpEntityGrouperChild annotation;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	protected JnGrouperChildEntityOnWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * The parents of the records, built when a write needs them: building them while the decorator chain is built would
	 * load the entity of the parent ids in the middle of the initialization of the grouped entity.
	 * @return the parents configured for the entity
	 */
	protected JnEntityGrouperParents getParents() {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		JnEntityGrouperParents parents = new JnEntityGrouperParents(this.annotation, entityDetails.configurationClass);
		return parents;
	}

	/**
	 * Whether the entity has a twin ({@code @CcpEntityTwin}).
	 * @return {@code true} for a twin entity
	 */
	protected boolean hasTwin() {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		boolean hasTwin = entityDetails.configurationClass.isAnnotationPresent(CcpEntityTwin.class);
		return hasTwin;
	}

	/**
	 * Whether this decorator is on the twin side of the entity.
	 * @return {@code true} on the twin side
	 */
	protected boolean isTwinSide() {
		boolean hasNoTwin = false == this.hasTwin();

		if(hasNoTwin) {
			return false;
		}

		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpEntityTwin twinAnnotation = entityDetails.configurationClass.getAnnotation(CcpEntityTwin.class);
		String twinEntityName = twinAnnotation.twinEntityName();
		boolean isTwinSide = twinEntityName.equals(entityDetails.entityName);
		return isTwinSide;
	}

	/**
	 * The main side of the entity, with every decorator.
	 * @return the {@code ENTITY} of the configurator class
	 */
	private CcpEntity getMainEntity() {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpReflectionConstructorDecorator configuratorConstructor = new CcpReflectionConstructorDecorator(entityDetails.configurationClass);
		CcpEntityConfigurator configurator = configuratorConstructor.newInstance();
		CcpEntity mainEntity = configurator.getEntity();
		return mainEntity;
	}

	/**
	 * A side of the entity, with every decorator.
	 * @param twinSide whether the twin side is wanted
	 * @return the main entity or its twin
	 */
	protected CcpEntity getSideEntity(boolean twinSide) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntity sideEntity = twinSide ? mainEntity.getTwinEntity() : mainEntity;
		return sideEntity;
	}

	/**
	 * Puts the record in the groups of its parents, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param record the record, its primary key already transformed
	 */
	protected void addToGroups(boolean twinSide, CcpJsonRepresentation record) {
		JnEntityGrouperParents parents = this.getParents();
		Map<String, CcpJsonRepresentation> recordParents = parents.getParents(record);
		this.addToGroups(twinSide, record, parents, recordParents);
	}

	/**
	 * Puts the record in the groups of parents already calculated, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param record the record, its primary key already transformed
	 * @param parents the parents configured for the entity
	 * @param recordParents the parents of the record, as {@code JnEntityGrouperParents.getParents} returns them
	 */
	protected void addToGroups(boolean twinSide, CcpJsonRepresentation record, JnEntityGrouperParents parents, Map<String, CcpJsonRepresentation> recordParents) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		String childId = mainEntity.calculateId(record);
		String parentName = parents.getParentName();
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		JnGroupers.addChild(grouper, parentName, mainDetails.entityName, childId, recordParents);
	}

	/**
	 * Takes the stored record out of the groups it is in, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param storedRecord the record as it is stored
	 */
	protected void removeFromGroups(boolean twinSide, CcpJsonRepresentation storedRecord) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		String childId = mainEntity.calculateId(storedRecord);
		JnEntityGrouperParents parents = this.getParents();
		String parentName = parents.getParentName();
		List<String> parentIds = parents.getStoredParentIds(storedRecord);
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		JnGroupers.removeChild(grouper, parentName, mainDetails.entityName, childId, parentIds);
	}

	/**
	 * Takes the record out of the groups of a side, when it is stored on that side of the entity (an entity without twin
	 * has no twin side).
	 * @param twinSide whether the twin side is the one checked
	 * @param json the record (at least its key)
	 */
	protected void removeFromGroupsWhenStored(boolean twinSide, CcpJsonRepresentation json) {
		boolean noSuchSide = twinSide && false == this.hasTwin();

		if(noSuchSide) {
			return;
		}

		CcpEntity sideEntity = this.getSideEntity(twinSide);
		boolean notStoredOnThisSide = false == sideEntity.exists(json);

		if(notStoredOnThisSide) {
			return;
		}

		CcpJsonRepresentation storedRecord = sideEntity.getOneById(json);
		this.removeFromGroups(twinSide, storedRecord);
	}
}
