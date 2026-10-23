package com.jn.entities.decorators.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.utils.JnGroupers;

/**
 * Decorator of {@code @CcpEntityGrouperChildCounter} (given as its {@code beforeDecoratorClass}) that counts the record
 * in the groups of {@code JnEntityGrouper} of its parents: the only child of each group is a single JSON text with one
 * counter per key ({@code childCount} is the number of keys), and the save adds 1 to the key of the record in the groups of the parents it was not in at its previous
 * write (all of them, on the insert). It stays in the outer part of the chain (priority 7), before the field
 * transformers, so the parents and the key are calculated from the values as they came.
 * <ul>
 * <li>save: reads the stored record, saves and then counts the record in its new parents. The parents of the previous
 * write are calculated from the stored record with its primary key as it came (the key is the same, and only the input
 * has it readable);</li>
 * <li>delete and deleteAnyWhere: the counters are a history and are left as they are.</li>
 * </ul>
 * The groups are always on the main side of the grouper. Two records counted in the same group at the same moment both
 * count: the database adds each one atomically.
 */
public class JnGrouperChildCounterEntityBeforeWrite extends CcpEntityDelegator {

	/** The counting configured for the entity. */
	private final CcpEntityGrouperChildCounter annotation;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the counting configured for the entity
	 */
	public JnGrouperChildCounterEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChildCounter annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * Saves the record and counts it, under its key, in the groups of the parents it was not in at its previous write.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		CcpJsonRepresentation previousVersion = this.getPreviousVersion(json);
		boolean inserted = this.entity.save(json);

		Class<?> counterKeyClass = this.annotation.counterKeyClass();
		CcpReflectionConstructorDecorator counterKeyConstructor = new CcpReflectionConstructorDecorator(counterKeyClass);
		Function<CcpJsonRepresentation, String> counterKeyProducer = counterKeyConstructor.newInstance();
		String counterKey = counterKeyProducer.apply(json);
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		Class<?>[] parentClasses = this.annotation.value();
		List<JnEntityGrouperParents> allParents = JnEntityGrouperParents.getAll(parentClasses, entityDetails.configurationClass);
		CcpEntity grouper = JnGroupers.getGrouper(false);
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> newParents = getNewParents(parents, json, previousVersion);
			String parentName = parents.getParentName();
			JnGroupers.incrementCounter(grouper, parentName, entityDetails.entityName, counterKey, newParents);
		}
		return inserted;
	}

	/**
	 * The record as it was before this write: the stored values with the primary key as it came.
	 * @param json the record, its fields not transformed yet
	 * @return the previous version, empty when the record was never written
	 */
	private CcpJsonRepresentation getPreviousVersion(CcpJsonRepresentation json) {
		CcpJsonRepresentation jsonWithHandledPrimaryKey = this.entity.getJsonWithHandledPrimaryKey(json);
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		// the transformers also keep the original values: only the transformed ones calculate the id
		CcpJsonRepresentation recordKey = entityDetails.getOnlyExistingFields(jsonWithHandledPrimaryKey);
		CcpJsonRepresentation storedRecord = entityDetails.getOneByIdOrHandleItIfThisIdWasNotFound(recordKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		boolean neverWritten = storedRecord.isEmpty();

		if(neverWritten) {
			return storedRecord;
		}

		CcpJsonRepresentation rawPrimaryKey = json.getJsonPiece(entityDetails.primaryKeyNames);
		CcpJsonRepresentation previousVersion = storedRecord.mergeWithAnotherJson(rawPrimaryKey);
		return previousVersion;
	}

	/**
	 * The parents of one kind the record enters with this write: the ones of the record now, but the ones of its previous
	 * version.
	 * @param parents the parents of one kind configured for the entity
	 * @param json the record, its fields not transformed yet
	 * @param previousVersion the record as it was, empty when it was never written
	 * @return the values of each new parent, by parent id
	 */
	private static Map<String, CcpJsonRepresentation> getNewParents(JnEntityGrouperParents parents, CcpJsonRepresentation json, CcpJsonRepresentation previousVersion) {
		Map<String, CcpJsonRepresentation> currentParents = parents.getParentsFromRawRecord(json);
		boolean neverWritten = previousVersion.isEmpty();

		if(neverWritten) {
			return currentParents;
		}

		Map<String, CcpJsonRepresentation> previousParents = parents.getParentsFromRawRecord(previousVersion);
		Set<String> previousParentIds = previousParents.keySet();
		Map<String, CcpJsonRepresentation> newParents = new LinkedHashMap<>(currentParents);
		Set<String> newParentIds = newParents.keySet();
		newParentIds.removeAll(previousParentIds);
		return newParents;
	}
}
