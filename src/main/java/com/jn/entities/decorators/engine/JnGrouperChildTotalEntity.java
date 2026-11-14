package com.jn.entities.decorators.engine;

import java.util.Map;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.jn.entities.JnEntityVirtualRecordsTotal;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;

/**
 * Decorator of {@code @CcpEntityGrouperChildTotal} (given as its {@code value}): keeps how many records each side of
 * the entity has now as the child of its group of {@link JnEntityVirtualRecordsTotal} in {@code JnEntityGrouper}, the
 * JSON text {@code {"total": n}}: the main side of the entity on the main side of the grouper, the twin side (when the
 * entity is a twin entity) on the twin side of the grouper. Each write reads by id, in a single union all straight from
 * the database, whether the record is on each side before and after it, and adds the difference to the total of each
 * side that changed (an insertion adds 1 to the main side; the deactivation and the reactivation move 1 between the
 * sides; the delete everywhere subtracts 1 from the side the record was on; an update changes nothing). The database
 * applies each change atomically, and a total that reaches 0 deletes its group.
 * <p>
 * It stays in the outer part of the chain (priority 7), before the field transformers, so the record comes with its key
 * as typed; the same decorator is in the chain of each side and handles both, whichever side the write starts on. The
 * record is looked for on the twin side by the same id of the main side, which holds for every twin entity but the
 * disposable ones (their twin id carries the period).
 */
public class JnGrouperChildTotalEntity extends CcpEntityDelegator {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 */
	public JnGrouperChildTotalEntity(CcpEntity entity) {
		super(entity);
	}

	/**
	 * Saves the record (in a twin entity, also takes it out of the twin side) and corrects the totals.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		CcpSelectUnionAll before = this.readBothSides(json);
		boolean inserted = this.entity.save(json);
		this.correctTheTotals(json, before);
		return inserted;
	}

	/**
	 * Deletes the record (in a twin entity, moves it to the other side) and corrects the totals.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		CcpSelectUnionAll before = this.readBothSides(json);
		boolean deleted = this.entity.delete(json);
		this.correctTheTotals(json, before);
		return deleted;
	}

	/**
	 * Deletes the record from both sides and corrects the totals.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		CcpSelectUnionAll before = this.readBothSides(json);
		boolean deleted = this.entity.deleteAnyWhere(json);
		this.correctTheTotals(json, before);
		return deleted;
	}

	/**
	 * Reads the record on both sides of the entity in a single union all, straight from the database (the keys of the
	 * record in the cache are deleted first).
	 * @param json the record, its fields not transformed yet
	 * @return the result of the union all
	 */
	private CcpSelectUnionAll readBothSides(CcpJsonRepresentation json) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpSelectUnionAll unionAll = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, mainEntity);
		return unionAll;
	}

	/**
	 * Adds, to the total of each side, the difference between where the record is now and where it was.
	 * @param json the record of the write
	 * @param before the record on both sides before the write
	 */
	private void correctTheTotals(CcpJsonRepresentation json, CcpSelectUnionAll before) {
		CcpSelectUnionAll after = this.readBothSides(json);
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		CcpJsonRepresentation jsonWithHandledPrimaryKey = mainEntity.getJsonWithHandledPrimaryKey(json);
		// the transformers also keep the original values: only the transformed ones calculate the id
		CcpJsonRepresentation recordKey = mainDetails.getOnlyExistingFields(jsonWithHandledPrimaryKey);
		String recordId = mainEntity.calculateId(recordKey);
		String mainEntityName = mainDetails.entityName;
		addTheDifference(false, mainEntityName, mainEntityName, recordId, before, after);
		Class<?> configurationClass = mainDetails.configurationClass;
		CcpEntityTwin twin = configurationClass.getAnnotation(CcpEntityTwin.class);
		boolean hasNoTwin = twin == null;

		if(hasNoTwin) {
			return;
		}

		String twinEntityName = twin.twinEntityName();
		addTheDifference(true, mainEntityName, twinEntityName, recordId, before, after);
	}

	/**
	 * Adds, to the total of one side, 1 when the record entered it with the write, or -1 when it left it.
	 * @param twinSide whether the side is the twin one (kept in the twin side of the grouper)
	 * @param mainEntityName the name of the main side, which names the group
	 * @param sideEntityName the name of the index of the side
	 * @param recordId the id of the record
	 * @param before the record on both sides before the write
	 * @param after the record on both sides after the write
	 */
	private static void addTheDifference(boolean twinSide, String mainEntityName, String sideEntityName, String recordId, CcpSelectUnionAll before, CcpSelectUnionAll after) {
		boolean presentBefore = before.isPresent(sideEntityName, recordId);
		boolean presentAfter = after.isPresent(sideEntityName, recordId);
		boolean unchanged = presentBefore == presentAfter;

		if(unchanged) {
			return;
		}

		int difference = presentAfter ? 1 : -1;
		String parentId = JnEntityVirtualRecordsTotal.getParentId(mainEntityName);
		CcpJsonRepresentation parentJson = JnEntityVirtualRecordsTotal.getParentJson(mainEntityName);
		CcpJsonRepresentation deltas = CcpOtherConstants.EMPTY_JSON.put(JnEntityVirtualRecordsTotal.Counters.total, difference);
		Map<String, CcpJsonRepresentation> deltasByParentId = Map.of(parentId, deltas);
		Map<String, CcpJsonRepresentation> parents = Map.of(parentId, parentJson);
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		CcpEntityMetaData parentDetails = JnEntityVirtualRecordsTotal.ENTITY.getEntityMetaData();
		JnGroupers.addToCounters(grouper, parentDetails.entityName, mainEntityName, deltasByParentId, parents);
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
