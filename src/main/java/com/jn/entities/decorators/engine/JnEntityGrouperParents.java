package com.jn.entities.decorators.engine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpStoredParentIdsField;

/**
 * The groups of {@code JnEntityGrouper} the records of an entity belong to, as {@code @CcpEntityGrouperChild} and
 * {@code @CcpStoredParentIdsField} configure them: the kind of group, the parents of a record (one per combination of its
 * values, each identified by the id of the combination) and the parents a stored record was written with. Any cost
 * center uses it through the annotations alone (the resumes of the vis, grouped by each search they satisfy, are the
 * first).
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
	 * @param configurationClass the configurator class of the grouped entity, whose fields hold the one annotated with
	 * {@code @CcpStoredParentIdsField}
	 */
	public JnEntityGrouperParents(CcpEntityGrouperChild annotation, Class<?> configurationClass) {
		Field storedParentIdsDeclaration = getStoredParentIdsDeclaration(configurationClass);
		CcpStoredParentIdsField storedParentIdsAnnotation = storedParentIdsDeclaration.getAnnotation(CcpStoredParentIdsField.class);
		CcpReflectionConstructorDecorator combinationsConstructor = new CcpReflectionConstructorDecorator(storedParentIdsAnnotation.value());
		this.parentsCombinations = combinationsConstructor.newInstance();
		// built from the annotations of the class, as its ENTITY is: the class does not have to be a configurator
		CcpEntityFactory parentIdEntityFactory = new CcpEntityFactory(annotation.value());
		this.parentIdEntity = parentIdEntityFactory.entityInstance;
		String storedParentIdsFieldName = storedParentIdsDeclaration.getName();
		this.storedParentIdsField = new CcpFieldName(storedParentIdsFieldName);
	}

	/**
	 * The only field of the entity annotated with {@code @CcpStoredParentIdsField}, which must be a list.
	 * @param configurationClass the configurator class of the grouped entity
	 * @return the declaration of the field
	 * @throws JnErrorStoredParentIdsFieldIsMissing when no field carries the annotation
	 * @throws JnErrorStoredParentIdsFieldIsDuplicated when more than one field carries it
	 * @throws JnErrorStoredParentIdsFieldIsNotAnArray when the field is not a list
	 */
	private static Field getStoredParentIdsDeclaration(Class<?> configurationClass) {
		Class<?> fieldsClass = CcpEntityFactory.getFieldsClass(configurationClass);
		Field[] declaredFields = fieldsClass.getDeclaredFields();
		List<Field> annotatedFields = new ArrayList<>();
		for (Field declaredField : declaredFields) {
			Class<?> declaredFieldType = declaredField.getType();
			boolean isNotAFieldName = false == CcpJsonFieldName.class.isAssignableFrom(declaredFieldType);

			if(isNotAFieldName) {
				continue;
			}

			boolean isNotAnnotated = false == declaredField.isAnnotationPresent(CcpStoredParentIdsField.class);

			if(isNotAnnotated) {
				continue;
			}

			annotatedFields.add(declaredField);
		}

		boolean noFieldAnnotated = annotatedFields.isEmpty();

		if(noFieldAnnotated) {
			throw new JnErrorStoredParentIdsFieldIsMissing(configurationClass);
		}

		int annotatedFieldsCount = annotatedFields.size();
		boolean moreThanOneFieldAnnotated = annotatedFieldsCount > 1;

		if(moreThanOneFieldAnnotated) {
			throw new JnErrorStoredParentIdsFieldIsDuplicated(configurationClass, annotatedFields);
		}

		Field storedParentIdsDeclaration = annotatedFields.get(0);
		boolean isNotAnArray = false == storedParentIdsDeclaration.isAnnotationPresent(CcpJsonFieldValidatorArray.class);

		if(isNotAnArray) {
			throw new JnErrorStoredParentIdsFieldIsNotAnArray(configurationClass, storedParentIdsDeclaration);
		}

		return storedParentIdsDeclaration;
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
	 * Fills the field annotated with {@code @CcpStoredParentIdsField} with the ids of the parents of the record, replacing
	 * whatever the input brought in it: the stored record tells, later, the groups it must leave.
	 * @param record the record as it is being written
	 * @param recordParents the parents of the record, as {@link #getParents} returns them
	 * @return the record with the ids of its parents
	 */
	public CcpJsonRepresentation putParentIds(CcpJsonRepresentation record, Map<String, CcpJsonRepresentation> recordParents) {
		Set<String> parentIdsSet = recordParents.keySet();
		List<String> parentIds = new ArrayList<>(parentIdsSet);
		CcpJsonRepresentation recordWithParentIds = record.put(this.storedParentIdsField, parentIds);
		return recordWithParentIds;
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

	/** Raised when no field of the grouped entity is annotated with {@code @CcpStoredParentIdsField}. */
	@SuppressWarnings("serial")
	private static class JnErrorStoredParentIdsFieldIsMissing extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 */
		private JnErrorStoredParentIdsFieldIsMissing(Class<?> configurationClass) {
			super("No field of " + configurationClass.getName() + " is annotated with @CcpStoredParentIdsField");
		}
	}

	/** Raised when more than one field of the grouped entity is annotated with {@code @CcpStoredParentIdsField}. */
	@SuppressWarnings("serial")
	private static class JnErrorStoredParentIdsFieldIsDuplicated extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 * @param annotatedFields the fields that carry the annotation
		 */
		private JnErrorStoredParentIdsFieldIsDuplicated(Class<?> configurationClass, List<Field> annotatedFields) {
			super("Only one field of " + configurationClass.getName() + " can be annotated with @CcpStoredParentIdsField, found: " + annotatedFields);
		}
	}

	/** Raised when the field annotated with {@code @CcpStoredParentIdsField} is not a list. */
	@SuppressWarnings("serial")
	private static class JnErrorStoredParentIdsFieldIsNotAnArray extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 * @param storedParentIdsDeclaration the field that carries the annotation
		 */
		private JnErrorStoredParentIdsFieldIsNotAnArray(Class<?> configurationClass, Field storedParentIdsDeclaration) {
			super("The field " + storedParentIdsDeclaration.getName() + " of " + configurationClass.getName() + " is annotated with @CcpStoredParentIdsField, so it must be a list (@CcpJsonFieldValidatorArray)");
		}
	}
}
