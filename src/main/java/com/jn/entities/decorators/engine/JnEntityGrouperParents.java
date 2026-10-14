package com.jn.entities.decorators.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.decorators.annotations.JnEntityGrouperChild;

/**
 * The groups of {@code JnEntityGrouper} the records of an entity belong to, as {@code @JnEntityGrouperChild} configures
 * them: the kind of group, the parents of a record (one per combination of its values, each identified by the id of
 * the combination) and the parents a stored record was written with. Any cost center uses it through the annotation
 * alone (the resumes of the vis, grouped by each search they satisfy, are the first).
 */
public class JnEntityGrouperParents {

	/** Produces, from a record, the combination of values of each parent. */
	private final Function<CcpJsonRepresentation, List<CcpJsonRepresentation>> parentsCombinations;

	/** Calculates the id of each parent from its combination. */
	private final CcpEntity parentIdEntity;

	/** The field of the record that keeps the ids of its parents. */
	private final CcpFieldName storedParentIdsField;

	/**
	 * Reads the grouping configured for the entity.
	 * @param annotation the grouping configured for the entity
	 */
	public JnEntityGrouperParents(JnEntityGrouperChild annotation) {
		CcpReflectionConstructorDecorator combinationsConstructor = new CcpReflectionConstructorDecorator(annotation.parentsCombinations());
		this.parentsCombinations = combinationsConstructor.newInstance();
		// built from the annotations of the class, as its ENTITY is: the class does not have to be a configurator
		CcpEntityFactory parentIdEntityFactory = new CcpEntityFactory(annotation.parentIdEntity());
		this.parentIdEntity = parentIdEntityFactory.entityInstance;
		this.storedParentIdsField = new CcpFieldName(annotation.storedParentIdsField());
	}

	/**
	 * The kind of group ({@code parentName} of {@code JnEntityGrouper}): the name of the entity that calculates the id of
	 * each parent.
	 * @return the parent name
	 */
	public String getParentName() {
		CcpEntityMetaData parentIdEntityDetails = this.parentIdEntity.getEntityMetaData();
		return parentIdEntityDetails.entityName;
	}

	/**
	 * The parents of a record, each with the combination of values its id came from ({@code parentJson}).
	 * @param record the record as it is being written (its fields already transformed)
	 * @return the combination of each parent, by parent id
	 */
	public Map<String, CcpJsonRepresentation> getParents(CcpJsonRepresentation record) {
		List<CcpJsonRepresentation> combinations = this.parentsCombinations.apply(record);
		Map<String, CcpJsonRepresentation> parentJsonByParentId = new LinkedHashMap<>();
		for (CcpJsonRepresentation combination : combinations) {
			String parentId = this.parentIdEntity.calculateId(combination);
			parentJsonByParentId.put(parentId, combination);
		}
		return parentJsonByParentId;
	}

	/**
	 * The parents a stored record belongs to, as they were when it was written: the groups it must leave.
	 * @param storedRecord the record as it is stored
	 * @return the parent ids
	 */
	public List<String> getStoredParentIds(CcpJsonRepresentation storedRecord) {
		List<String> storedParentIds = storedRecord.getAsStringList(this.storedParentIdsField);
		return storedParentIds;
	}
}
