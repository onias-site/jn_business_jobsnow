package com.jn.entities.decorators.engine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperParentCombinations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpStoredParentIdsField;

/**
 * The groups of {@code JnEntityGrouper} of one kind the records of an entity belong to, as {@code @CcpEntityGrouperChild}
 * (or {@code @CcpEntityGrouperChildCounter}) configures them: the kind of group (one class of the {@code value} of the
 * annotation) and the parents of a record (one per combination of its values, each identified by the id of the
 * combination). The parent is declared in one of two ways:
 * <ul>
 * <li><b>oneToMany</b>, by {@code @CcpEntityGrouperParentCombinations} on the entity: the parents come from the values
 * of the record itself, so the parents a stored record was written with are calculated again from it;</li>
 * <li><b>manyToMany</b>, by a list field annotated with {@code @CcpStoredParentIdsField}: the record keeps in that field
 * the ids of the parents it was written with.</li>
 * </ul>
 * An entity that is a child of several kinds of group has one instance per kind ({@link #getAll}). Any cost center
 * uses it through the annotations alone (the resumes of the vis, grouped by each search they satisfy, are the first).
 */
public class JnEntityGrouperParents {

	/** Produces, from a record, the combination of values of each parent. */
	private final Function<CcpJsonRepresentation, List<CcpJsonRepresentation>> parentsCombinations;

	/** Calculates the id of each parent from its combination. */
	private final CcpEntity parentIdEntity;

	/** The field of the record that keeps the ids of its parents (manyToMany); empty in a oneToMany. */
	private final Optional<CcpFieldName> storedParentIdsField;

	/**
	 * Reads the grouping of one kind configured for the entity.
	 * @param parentClass one class of the {@code value} of the grouping annotation: the parent entity
	 * @param configurationClass the configurator class of the grouped entity, which declares the parent by a
	 * {@code @CcpEntityGrouperParentCombinations} or by a field annotated with {@code @CcpStoredParentIdsField} whose
	 * {@code parent} is {@code parentClass}
	 */
	public JnEntityGrouperParents(Class<?> parentClass, Class<?> configurationClass) {
		List<Field> annotatedFields = getStoredParentIdsDeclarations(configurationClass);
		List<Field> fieldsOfTheParent = getStoredParentIdsDeclarationsOf(parentClass, annotatedFields);
		List<CcpEntityGrouperParentCombinations> combinationsOfTheParent = getCombinationsDeclarationsOf(parentClass, configurationClass);
		Class<?> combinationsClass = getCombinationsClass(configurationClass, parentClass, fieldsOfTheParent, combinationsOfTheParent);
		CcpReflectionConstructorDecorator combinationsConstructor = new CcpReflectionConstructorDecorator(combinationsClass);
		this.parentsCombinations = combinationsConstructor.newInstance();
		// built from the annotations of the class, as its ENTITY is: the class does not have to be a configurator
		CcpEntityFactory parentIdEntityFactory = new CcpEntityFactory(parentClass);
		this.parentIdEntity = parentIdEntityFactory.entityInstance;
		this.storedParentIdsField = getStoredParentIdsField(configurationClass, fieldsOfTheParent);
	}

	/**
	 * Every kind of group of the given parents, in the order they are declared.
	 * @param parentClasses the {@code value} of the grouping annotation
	 * @param configurationClass the configurator class of the grouped entity
	 * @return the parents of each kind
	 * @throws JnErrorParentDeclarationWithUnknownParent when a {@code @CcpStoredParentIdsField} or a
	 * {@code @CcpEntityGrouperParentCombinations} has a {@code parent} that is in the {@code value} of no grouping
	 * annotation of the entity
	 */
	public static List<JnEntityGrouperParents> getAll(Class<?>[] parentClasses, Class<?> configurationClass) {
		List<Class<?>> knownParents = getParentsOfEveryGrouping(configurationClass);
		List<Field> annotatedFields = getStoredParentIdsDeclarations(configurationClass);
		for (Field annotatedField : annotatedFields) {
			CcpStoredParentIdsField storedParentIdsAnnotation = annotatedField.getAnnotation(CcpStoredParentIdsField.class);
			Class<?> parentClass = storedParentIdsAnnotation.parent();
			boolean knownParent = knownParents.contains(parentClass);

			if(knownParent) {
				continue;
			}

			String fieldName = annotatedField.getName();
			throw new JnErrorParentDeclarationWithUnknownParent(configurationClass, fieldName, parentClass);
		}

		CcpEntityGrouperParentCombinations[] combinationsDeclarations = configurationClass.getAnnotationsByType(CcpEntityGrouperParentCombinations.class);
		for (CcpEntityGrouperParentCombinations combinationsDeclaration : combinationsDeclarations) {
			Class<?> parentClass = combinationsDeclaration.parent();
			boolean knownParent = knownParents.contains(parentClass);

			if(knownParent) {
				continue;
			}

			throw new JnErrorParentDeclarationWithUnknownParent(configurationClass, Constants.COMBINATIONS_ANNOTATION.getValue(), parentClass);
		}

		List<JnEntityGrouperParents> allParents = new ArrayList<>();
		for (Class<?> parentClass : parentClasses) {
			JnEntityGrouperParents parents = new JnEntityGrouperParents(parentClass, configurationClass);
			allParents.add(parents);
		}
		return allParents;
	}

	/** The texts of this class. */
	enum Constants implements CcpJsonFieldName {
		/** How a {@code @CcpEntityGrouperParentCombinations} is named in the errors. */
		COMBINATIONS_ANNOTATION {
			public String getValue() {
				return "@CcpEntityGrouperParentCombinations";
			}
		},
	}

	/**
	 * The parents of every grouping annotation of the entity ({@code @CcpEntityGrouperChild} and
	 * {@code @CcpEntityGrouperChildCounter}).
	 * @param configurationClass the configurator class of the grouped entity
	 * @return the parent classes
	 */
	private static List<Class<?>> getParentsOfEveryGrouping(Class<?> configurationClass) {
		List<Class<?>> parents = new ArrayList<>();
		CcpEntityGrouperChild[] groupings = configurationClass.getAnnotationsByType(CcpEntityGrouperChild.class);
		for (CcpEntityGrouperChild grouping : groupings) {
			Class<?>[] groupingParents = grouping.value();
			List<Class<?>> groupingParentsList = List.of(groupingParents);
			parents.addAll(groupingParentsList);
		}
		CcpEntityGrouperChildCounter[] counters = configurationClass.getAnnotationsByType(CcpEntityGrouperChildCounter.class);
		for (CcpEntityGrouperChildCounter counter : counters) {
			Class<?>[] counterParents = counter.value();
			List<Class<?>> counterParentsList = List.of(counterParents);
			parents.addAll(counterParentsList);
		}
		return parents;
	}

	/**
	 * The fields of the entity annotated with {@code @CcpStoredParentIdsField}, whatever their parent.
	 * @param configurationClass the configurator class of the grouped entity
	 * @return the declarations of the fields
	 */
	private static List<Field> getStoredParentIdsDeclarations(Class<?> configurationClass) {
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
		return annotatedFields;
	}

	/**
	 * The fields annotated with {@code @CcpStoredParentIdsField} whose {@code parent} is the given class.
	 * @param parentClass the parent entity
	 * @param annotatedFields the fields of the entity annotated with {@code @CcpStoredParentIdsField}
	 * @return the declarations of the fields of that parent
	 */
	private static List<Field> getStoredParentIdsDeclarationsOf(Class<?> parentClass, List<Field> annotatedFields) {
		List<Field> fieldsOfTheParent = new ArrayList<>();
		for (Field annotatedField : annotatedFields) {
			CcpStoredParentIdsField storedParentIdsAnnotation = annotatedField.getAnnotation(CcpStoredParentIdsField.class);
			Class<?> fieldParentClass = storedParentIdsAnnotation.parent();
			boolean anotherParent = false == parentClass.equals(fieldParentClass);

			if(anotherParent) {
				continue;
			}

			fieldsOfTheParent.add(annotatedField);
		}
		return fieldsOfTheParent;
	}

	/**
	 * The {@code @CcpEntityGrouperParentCombinations} of the entity whose {@code parent} is the given class.
	 * @param parentClass the parent entity
	 * @param configurationClass the configurator class of the grouped entity
	 * @return the declarations of that parent
	 */
	private static List<CcpEntityGrouperParentCombinations> getCombinationsDeclarationsOf(Class<?> parentClass, Class<?> configurationClass) {
		CcpEntityGrouperParentCombinations[] combinationsDeclarations = configurationClass.getAnnotationsByType(CcpEntityGrouperParentCombinations.class);
		List<CcpEntityGrouperParentCombinations> combinationsOfTheParent = new ArrayList<>();
		for (CcpEntityGrouperParentCombinations combinationsDeclaration : combinationsDeclarations) {
			Class<?> declarationParentClass = combinationsDeclaration.parent();
			boolean anotherParent = false == parentClass.equals(declarationParentClass);

			if(anotherParent) {
				continue;
			}

			combinationsOfTheParent.add(combinationsDeclaration);
		}
		return combinationsOfTheParent;
	}

	/**
	 * The class that produces the combinations of the parent, from its only declaration.
	 * @param configurationClass the configurator class of the grouped entity
	 * @param parentClass the parent entity
	 * @param fieldsOfTheParent the fields annotated with {@code @CcpStoredParentIdsField} of that parent
	 * @param combinationsOfTheParent the {@code @CcpEntityGrouperParentCombinations} of that parent
	 * @return the class of the combinations
	 * @throws JnErrorParentDeclarationIsMissing when the parent is declared in no way
	 * @throws JnErrorParentDeclarationIsDuplicated when it is declared more than once
	 */
	private static Class<?> getCombinationsClass(Class<?> configurationClass, Class<?> parentClass, List<Field> fieldsOfTheParent, List<CcpEntityGrouperParentCombinations> combinationsOfTheParent) {
		List<String> declarations = new ArrayList<>();
		for (Field fieldOfTheParent : fieldsOfTheParent) {
			String fieldName = fieldOfTheParent.getName();
			declarations.add(fieldName);
		}
		for (int index = 0; index < combinationsOfTheParent.size(); index++) {
			String combinationsAnnotation = Constants.COMBINATIONS_ANNOTATION.getValue();
			declarations.add(combinationsAnnotation);
		}

		boolean notDeclared = declarations.isEmpty();

		if(notDeclared) {
			throw new JnErrorParentDeclarationIsMissing(configurationClass, parentClass);
		}

		int declarationsCount = declarations.size();
		boolean declaredMoreThanOnce = declarationsCount > 1;

		if(declaredMoreThanOnce) {
			throw new JnErrorParentDeclarationIsDuplicated(configurationClass, parentClass, declarations);
		}

		boolean oneToMany = fieldsOfTheParent.isEmpty();

		if(oneToMany) {
			CcpEntityGrouperParentCombinations combinationsDeclaration = combinationsOfTheParent.get(0);
			return combinationsDeclaration.value();
		}

		Field storedParentIdsDeclaration = fieldsOfTheParent.get(0);
		CcpStoredParentIdsField storedParentIdsAnnotation = storedParentIdsDeclaration.getAnnotation(CcpStoredParentIdsField.class);
		return storedParentIdsAnnotation.value();
	}

	/**
	 * The field that keeps the ids of the parents, which must be a list; none in a oneToMany.
	 * @param configurationClass the configurator class of the grouped entity
	 * @param fieldsOfTheParent the fields annotated with {@code @CcpStoredParentIdsField} of the parent (one at most)
	 * @return the field, empty in a oneToMany
	 * @throws JnErrorStoredParentIdsFieldIsNotAnArray when the field is not a list
	 */
	private static Optional<CcpFieldName> getStoredParentIdsField(Class<?> configurationClass, List<Field> fieldsOfTheParent) {
		boolean oneToMany = fieldsOfTheParent.isEmpty();

		if(oneToMany) {
			return Optional.empty();
		}

		Field storedParentIdsDeclaration = fieldsOfTheParent.get(0);
		boolean isNotAnArray = false == storedParentIdsDeclaration.isAnnotationPresent(CcpJsonFieldValidatorArray.class);

		if(isNotAnArray) {
			throw new JnErrorStoredParentIdsFieldIsNotAnArray(configurationClass, storedParentIdsDeclaration);
		}

		String storedParentIdsFieldName = storedParentIdsDeclaration.getName();
		CcpFieldName storedParentIdsFieldDecorator = new CcpFieldName(storedParentIdsFieldName);
		return Optional.of(storedParentIdsFieldDecorator);
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
	 * The parents of a record that did not go through the field transformers yet: each combination has its primary key
	 * transformed as the entity of the parent ids does it (the e-mail becomes its SHA-1, for one) and keeps only the fields
	 * of that entity, so no original value of a transformed field reaches the group.
	 * @param rawRecord the record as it came, its fields not transformed
	 * @return the combination of each parent, by parent id
	 */
	public Map<String, CcpJsonRepresentation> getParentsFromRawRecord(CcpJsonRepresentation rawRecord) {
		List<CcpJsonRepresentation> combinations = this.parentsCombinations.apply(rawRecord);
		CcpEntityMetaData parentIdEntityDetails = this.parentIdEntity.getEntityMetaData();
		Map<String, CcpJsonRepresentation> parentJsonByParentId = new LinkedHashMap<>();
		for (CcpJsonRepresentation combination : combinations) {
			CcpJsonRepresentation handledCombination = this.parentIdEntity.getJsonWithHandledPrimaryKey(combination);
			CcpJsonRepresentation parentJson = parentIdEntityDetails.getOnlyExistingFields(handledCombination);
			String parentId = this.parentIdEntity.calculateId(parentJson);
			parentJsonByParentId.put(parentId, parentJson);
		}
		return parentJsonByParentId;
	}

	/**
	 * Fills the field annotated with {@code @CcpStoredParentIdsField} with the ids of the parents of the record, replacing
	 * whatever the input brought in it: the stored record tells, later, the groups it must leave. A oneToMany has no such
	 * field, and the record is returned as it is.
	 * @param record the record as it is being written
	 * @param recordParents the parents of the record, as {@link #getParents} returns them
	 * @return the record with the ids of its parents
	 */
	public CcpJsonRepresentation putParentIds(CcpJsonRepresentation record, Map<String, CcpJsonRepresentation> recordParents) {
		boolean oneToMany = this.storedParentIdsField.isEmpty();

		if(oneToMany) {
			return record;
		}

		CcpFieldName storedParentIdsFieldName = this.storedParentIdsField.get();
		Set<String> parentIdsSet = recordParents.keySet();
		List<String> parentIds = new ArrayList<>(parentIdsSet);
		CcpJsonRepresentation recordWithParentIds = record.put(storedParentIdsFieldName, parentIds);
		return recordWithParentIds;
	}

	/**
	 * The record without the field annotated with {@code @CcpStoredParentIdsField} (as it is, in a oneToMany).
	 * @param record the record
	 * @return the record without the ids of its parents
	 */
	public CcpJsonRepresentation removeParentIds(CcpJsonRepresentation record) {
		boolean oneToMany = this.storedParentIdsField.isEmpty();

		if(oneToMany) {
			return record;
		}

		CcpFieldName storedParentIdsFieldName = this.storedParentIdsField.get();
		CcpJsonRepresentation recordWithoutParentIds = record.removeFields(storedParentIdsFieldName);
		return recordWithoutParentIds;
	}

	/**
	 * The parents a stored record belongs to, as they were when it was written: the groups it must leave. In a oneToMany
	 * they are calculated again from the stored record; in a manyToMany they are the ids kept in its field.
	 * @param storedRecord the record as it is stored
	 * @return the parent ids
	 */
	public List<String> getStoredParentIds(CcpJsonRepresentation storedRecord) {
		boolean oneToMany = this.storedParentIdsField.isEmpty();

		if(oneToMany) {
			Map<String, CcpJsonRepresentation> storedParents = this.getParents(storedRecord);
			Set<String> storedParentIds = storedParents.keySet();
			return new ArrayList<>(storedParentIds);
		}

		CcpFieldName storedParentIdsFieldName = this.storedParentIdsField.get();
		List<String> storedParentIds = storedRecord.getAsStringList(storedParentIdsFieldName);
		return storedParentIds;
	}

	/** Raised when the grouped entity declares a parent in no way. */
	@SuppressWarnings("serial")
	private static class JnErrorParentDeclarationIsMissing extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 * @param parentClass the parent entity that is not declared
		 */
		private JnErrorParentDeclarationIsMissing(Class<?> configurationClass, Class<?> parentClass) {
			super(configurationClass.getName() + " declares the parent " + parentClass.getName() + " neither by @CcpEntityGrouperParentCombinations(parent = "
					+ parentClass.getName() + ".class) (oneToMany) nor by a field annotated with @CcpStoredParentIdsField(parent = " + parentClass.getName() + ".class) (manyToMany)");
		}
	}

	/** Raised when the grouped entity declares a parent more than once. */
	@SuppressWarnings("serial")
	private static class JnErrorParentDeclarationIsDuplicated extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 * @param parentClass the parent entity declared more than once
		 * @param declarations the declarations found (field names or the combinations annotation)
		 */
		private JnErrorParentDeclarationIsDuplicated(Class<?> configurationClass, Class<?> parentClass, List<String> declarations) {
			super(configurationClass.getName() + " must declare the parent " + parentClass.getName() + " only once, by @CcpEntityGrouperParentCombinations or by a field annotated with @CcpStoredParentIdsField, found: " + declarations);
		}
	}

	/**
	 * Raised when the {@code parent} of a {@code @CcpStoredParentIdsField} or of a
	 * {@code @CcpEntityGrouperParentCombinations} is in the {@code value} of no grouping annotation of the entity.
	 */
	@SuppressWarnings("serial")
	private static class JnErrorParentDeclarationWithUnknownParent extends RuntimeException {
		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the grouped entity
		 * @param declaration the declaration of the parent (a field name or the combinations annotation)
		 * @param parentClass the parent of the declaration
		 */
		private JnErrorParentDeclarationWithUnknownParent(Class<?> configurationClass, String declaration, Class<?> parentClass) {
			super("The declaration " + declaration + " of " + configurationClass.getName() + " has the parent " + parentClass.getName()
					+ ", but that class is in the value of no @CcpEntityGrouperChild nor @CcpEntityGrouperChildCounter");
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
