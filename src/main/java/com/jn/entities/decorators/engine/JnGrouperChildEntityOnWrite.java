package com.jn.entities.decorators.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * Base shared by the decorators that keep the records of an entity ({@code @CcpEntityGrouperChild}) in the groups of
 * {@code JnEntityGrouper}. Each side of the entity has its side of the grouper: the main records are children of the main
 * groups, the twin records of the twin groups. The child is the id of the record, the same on both sides, or, when the
 * annotation names {@code childFields}, a JSON text with those fields of the record and its id in {@code id}; the child
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
	 * load the entities of the parent ids in the middle of the initialization of the grouped entity.
	 * @return the parents configured for the entity, one per kind of group
	 */
	protected List<JnEntityGrouperParents> getAllParents() {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		Class<?>[] parentClasses = this.annotation.value();
		List<JnEntityGrouperParents> allParents = JnEntityGrouperParents.getAll(parentClasses, entityDetails.configurationClass);
		return allParents;
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
	 * Puts the record in the groups of its parents of every kind, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param record the record, its primary key already transformed
	 */
	protected void addToGroups(boolean twinSide, CcpJsonRepresentation record) {
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParents(record);
			this.addToGroups(twinSide, record, parents, recordParents);
		}
	}

	/**
	 * Puts the record in the groups of parents of one kind already calculated, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param record the record, its primary key already transformed
	 * @param parents the parents of one kind configured for the entity
	 * @param recordParents the parents of the record, as {@code JnEntityGrouperParents.getParents} returns them
	 */
	protected void addToGroups(boolean twinSide, CcpJsonRepresentation record, JnEntityGrouperParents parents, Map<String, CcpJsonRepresentation> recordParents) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		String child = this.getChild(mainEntity, record);
		String parentName = parents.getParentName();
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		JnGroupers.addChild(grouper, parentName, mainDetails.entityName, child, recordParents);
	}

	/**
	 * What the record keeps in its groups: its id or, when the annotation names {@code childFields}, a JSON text with
	 * those fields of the record, the fields its {@code childFieldsProducers} calculate and its id in {@code id}.
	 * @param mainEntity the main side of the entity
	 * @param record the record, its fields already transformed
	 * @return the child
	 */
	protected String getChild(CcpEntity mainEntity, CcpJsonRepresentation record) {
		String childId = mainEntity.calculateId(record);
		String[] childFields = this.annotation.childFields();
		boolean childIsTheId = childFields.length == 0;

		if(childIsTheId) {
			return childId;
		}

		List<String> childFieldsList = Arrays.asList(childFields);
		CcpJsonRepresentation childRecord = record.getJsonPiece(childFieldsList);
		Class<?>[] childFieldsProducers = this.annotation.childFieldsProducers();
		for (Class<?> childFieldsProducerClass : childFieldsProducers) {
			CcpReflectionConstructorDecorator producerConstructor = new CcpReflectionConstructorDecorator(childFieldsProducerClass);
			Function<CcpJsonRepresentation, CcpJsonRepresentation> childFieldsProducer = producerConstructor.newInstance();
			CcpJsonRepresentation producedFields = childFieldsProducer.apply(record);
			childRecord = childRecord.mergeWithAnotherJson(producedFields);
		}
		CcpJsonRepresentation childRecordWithId = childRecord.put(JnJsonCommonsFields.id, childId);
		String child = childRecordWithId.asUgglyJson();
		return child;
	}

	/**
	 * Takes the stored record out of the groups it is in, of every kind, on the given side of the grouper.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param storedRecord the record as it is stored
	 */
	protected void removeFromGroups(boolean twinSide, CcpJsonRepresentation storedRecord) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		String childId = mainEntity.calculateId(storedRecord);
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		for (JnEntityGrouperParents parents : allParents) {
			String parentName = parents.getParentName();
			List<String> parentIds = parents.getStoredParentIds(storedRecord);
			this.removeChild(grouper, parentName, mainDetails.entityName, childId, parentIds);
		}
	}

	/**
	 * Takes the child of a record out of the groups of some parents: by its id or, when the child is a JSON text
	 * ({@code childFields}), by the text {@code "id":"<id>"} that only the children of that record contain.
	 * @param grouper the side of the grouper written
	 * @param parentName the kind of group
	 * @param childName the name of the main entity
	 * @param childId the id of the record
	 * @param parentIds the parent ids
	 */
	private void removeChild(CcpEntity grouper, String parentName, String childName, String childId, List<String> parentIds) {
		String[] childFields = this.annotation.childFields();
		boolean childIsTheId = childFields.length == 0;

		if(childIsTheId) {
			JnGroupers.removeChild(grouper, parentName, childName, childId, parentIds);
			return;
		}

		CcpJsonRepresentation idAlone = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.id, childId);
		String idAloneText = idAlone.asUgglyJson();
		int closingBrace = idAloneText.length() - 1;
		String childKey = idAloneText.substring(1, closingBrace);
		List<String> childKeys = Arrays.asList(childKey);
		JnGroupers.removeChildByKeys(grouper, parentName, childName, childKeys, parentIds);
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
