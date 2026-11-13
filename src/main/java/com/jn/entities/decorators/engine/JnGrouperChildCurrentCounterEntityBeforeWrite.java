package com.jn.entities.decorators.engine;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.jn.utils.JnGroupers;

/**
 * Decorator of {@code @CcpEntityGrouperChildCounter} (given as its {@code beforeDecoratorClass}) whose counters are the
 * current state, not a history: the counter of a key, in the group of a parent, is how many records stored on the main
 * side of the entity have that key now. Unlike {@code JnGrouperChildCounterEntityBeforeWrite}, a record may have several
 * keys (the {@code counterKeyClass} produces a {@code List<String>}, each key without quotes), and every write corrects
 * the counters: the record stored on the main side is read before and after the write, its keys before are subtracted
 * and its keys after are added (in the groups of its parents before and after), so a key dropped by a save, the
 * deactivation (the record leaves the main side) and the delete everywhere subtract 1, and the reactivation adds it
 * back. A key that reaches 0 leaves the group, and a group with no key is deleted.
 * <p>
 * The parents and the keys are calculated from the stored record (its fields as stored, so the key field must have no
 * transformer that hides its value). The groups are always on the main side of the grouper. Two records counted in the
 * same group at the same moment both count: the database applies each script atomically.
 */
public class JnGrouperChildCurrentCounterEntityBeforeWrite extends CcpEntityDelegator {

	/** The counting configured for the entity. */
	private final CcpEntityGrouperChildCounter annotation;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the counting configured for the entity
	 */
	public JnGrouperChildCurrentCounterEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChildCounter annotation) {
		super(entity);
		this.annotation = annotation;
	}

	/**
	 * Saves the record and corrects the counters by the keys it had and has on the main side.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		CcpJsonRepresentation storedBefore = this.getStoredOnTheMainSide(json);
		boolean inserted = this.entity.save(json);
		this.correctCounters(json, storedBefore);
		return inserted;
	}

	/**
	 * Deletes the record (in a twin entity, moves it to the other side) and corrects the counters.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation storedBefore = this.getStoredOnTheMainSide(json);
		boolean deleted = this.entity.delete(json);
		this.correctCounters(json, storedBefore);
		return deleted;
	}

	/**
	 * Deletes the record from both sides and corrects the counters.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation storedBefore = this.getStoredOnTheMainSide(json);
		boolean deleted = this.entity.deleteAnyWhere(json);
		this.correctCounters(json, storedBefore);
		return deleted;
	}

	/**
	 * Subtracts the keys of the record as it was on the main side and adds the keys it has there now.
	 * @param json the record of the write
	 * @param storedBefore the record stored on the main side before the write, empty when it was not there
	 */
	private void correctCounters(CcpJsonRepresentation json, CcpJsonRepresentation storedBefore) {
		CcpJsonRepresentation storedAfter = this.getStoredOnTheMainSide(json);
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		Class<?>[] parentClasses = this.annotation.value();
		List<JnEntityGrouperParents> allParents = JnEntityGrouperParents.getAll(parentClasses, mainDetails.configurationClass);
		CcpEntity grouper = JnGroupers.getGrouper(false);
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> parentJsonByParentId = new LinkedHashMap<>();
			Map<String, Map<String, Integer>> deltasByParentId = new LinkedHashMap<>();
			this.addDeltas(parents, storedBefore, -1, parentJsonByParentId, deltasByParentId);
			this.addDeltas(parents, storedAfter, 1, parentJsonByParentId, deltasByParentId);
			Map<String, CcpJsonRepresentation> changedDeltasByParentId = getChangedDeltas(deltasByParentId);
			boolean nothingChanged = changedDeltasByParentId.isEmpty();

			if(nothingChanged) {
				continue;
			}

			String parentName = parents.getParentName();
			JnGroupers.addToCounters(grouper, parentName, mainDetails.entityName, changedDeltasByParentId, parentJsonByParentId);
		}
	}

	/**
	 * Adds, to the deltas of each parent of the stored record, the amount of each of its keys.
	 * @param parents the parents of one kind configured for the entity
	 * @param storedRecord the record as stored on the main side, empty when it is not there
	 * @param amount -1 for the record before the write, 1 for the record after it
	 * @param parentJsonByParentId receives the values of each parent
	 * @param deltasByParentId receives the amount of each key, by parent id
	 */
	private void addDeltas(JnEntityGrouperParents parents, CcpJsonRepresentation storedRecord, int amount, Map<String, CcpJsonRepresentation> parentJsonByParentId, Map<String, Map<String, Integer>> deltasByParentId) {
		boolean notStored = storedRecord.isEmpty();

		if(notStored) {
			return;
		}

		Set<String> keys = this.getKeys(storedRecord);
		Map<String, CcpJsonRepresentation> recordParents = parents.getParents(storedRecord);
		parentJsonByParentId.putAll(recordParents);
		Set<String> parentIds = recordParents.keySet();
		for (String parentId : parentIds) {
			Map<String, Integer> deltas = deltasByParentId.computeIfAbsent(parentId, id -> new LinkedHashMap<>());
			for (String key : keys) {
				deltas.merge(key, amount, Integer::sum);
			}
		}
	}

	/**
	 * The deltas that change some counter, as the JSON the script receives.
	 * @param deltasByParentId the amount of each key, by parent id
	 * @return the keys whose amount is not 0, by parent id; a parent with none is left out
	 */
	private static Map<String, CcpJsonRepresentation> getChangedDeltas(Map<String, Map<String, Integer>> deltasByParentId) {
		Map<String, CcpJsonRepresentation> changedDeltasByParentId = new LinkedHashMap<>();
		Set<String> parentIds = deltasByParentId.keySet();
		for (String parentId : parentIds) {
			Map<String, Integer> deltas = deltasByParentId.get(parentId);
			CcpJsonRepresentation changedDeltas = CcpOtherConstants.EMPTY_JSON;
			Set<String> keys = deltas.keySet();
			for (String key : keys) {
				int delta = deltas.get(key);
				boolean unchanged = delta == 0;

				if(unchanged) {
					continue;
				}

				CcpFieldName keyFieldName = new CcpFieldName(key);
				changedDeltas = changedDeltas.put(keyFieldName, delta);
			}
			boolean noChange = changedDeltas.isEmpty();

			if(noChange) {
				continue;
			}

			changedDeltasByParentId.put(parentId, changedDeltas);
		}
		return changedDeltasByParentId;
	}

	/**
	 * The keys of the stored record, each once.
	 * @param storedRecord the record as stored
	 * @return the keys
	 */
	private Set<String> getKeys(CcpJsonRepresentation storedRecord) {
		Class<?> counterKeyClass = this.annotation.counterKeyClass();
		CcpReflectionConstructorDecorator counterKeyConstructor = new CcpReflectionConstructorDecorator(counterKeyClass);
		Function<CcpJsonRepresentation, List<String>> counterKeysProducer = counterKeyConstructor.newInstance();
		List<String> keys = counterKeysProducer.apply(storedRecord);
		Set<String> distinctKeys = new LinkedHashSet<>(keys);
		return distinctKeys;
	}

	/**
	 * The record as stored on the main side of the entity, read by id straight from the database (no cache).
	 * @param json the record (at least its key, not transformed yet)
	 * @return the stored record, empty when it is not on the main side
	 */
	private CcpJsonRepresentation getStoredOnTheMainSide(CcpJsonRepresentation json) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpJsonRepresentation jsonWithHandledPrimaryKey = mainEntity.getJsonWithHandledPrimaryKey(json);
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		// the transformers also keep the original values: only the transformed ones calculate the id
		CcpJsonRepresentation recordKey = mainDetails.getOnlyExistingFields(jsonWithHandledPrimaryKey);
		CcpJsonRepresentation storedRecord = mainDetails.getOneByIdOrHandleItIfThisIdWasNotFound(recordKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		return storedRecord;
	}

	/**
	 * The main side of the entity, with every decorator, whichever side this decorator is on.
	 * @return the {@code ENTITY} of the configurator class
	 */
	private CcpEntity getMainEntity() {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpReflectionConstructorDecorator configuratorConstructor = new CcpReflectionConstructorDecorator(entityDetails.configurationClass);
		CcpEntityConfigurator configurator = configuratorConstructor.newInstance();
		CcpEntity mainEntity = configurator.getEntity();
		return mainEntity;
	}
}
