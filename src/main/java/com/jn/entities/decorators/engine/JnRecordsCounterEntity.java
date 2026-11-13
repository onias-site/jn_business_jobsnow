package com.jn.entities.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.jn.entities.JnEntityRecordsCount;

/**
 * Decorator of {@code @CcpEntityRecordsCounter} (given as its {@code value}): after each save (the insertion), delete
 * (the deactivation on the main side, the reactivation on the twin side) and delete everywhere, refreshes both sides of
 * the entity in the database, counts their records again and stores the amounts in {@link JnEntityRecordsCount}, one
 * record per entity. Counting again, instead of adding and subtracting, keeps the amounts right whatever happened
 * before (records written before the decorator existed, a write that failed halfway).
 */
public class JnRecordsCounterEntity extends CcpEntityDelegator {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 */
	public JnRecordsCounterEntity(CcpEntity entity) {
		super(entity);
	}

	/**
	 * Saves the record and counts the sides again.
	 * @param json the record
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		boolean inserted = this.entity.save(json);
		this.countTheRecords();
		return inserted;
	}

	/**
	 * Deletes the record (in a twin entity, moves it to the other side) and counts the sides again.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		boolean deleted = this.entity.delete(json);
		this.countTheRecords();
		return deleted;
	}

	/**
	 * Deletes the record from both sides and counts the sides again.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean deleted = this.entity.deleteAnyWhere(json);
		this.countTheRecords();
		return deleted;
	}

	/** Counts the records of the main and of the twin side and stores the amounts. */
	private void countTheRecords() {
		CcpEntity mainEntity = this.getMainEntity();
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		String mainEntityName = mainDetails.entityName;
		Class<?> configurationClass = mainDetails.configurationClass;
		CcpEntityTwin twin = configurationClass.getAnnotation(CcpEntityTwin.class);
		boolean hasNoTwin = twin == null;
		String twinEntityName = hasNoTwin ? mainEntityName : twin.twinEntityName();
		long mainRecords = countRecords(mainEntityName);
		long twinRecords = hasNoTwin ? 0L : countRecords(twinEntityName);
		CcpJsonRepresentation countWithEntityName = CcpOtherConstants.EMPTY_JSON.put(JnEntityRecordsCount.Fields.entityName, mainEntityName);
		CcpJsonRepresentation countWithTwinEntityName = countWithEntityName.put(JnEntityRecordsCount.Fields.twinEntityName, twinEntityName);
		CcpJsonRepresentation countWithMainRecords = countWithTwinEntityName.put(JnEntityRecordsCount.Fields.mainRecords, mainRecords);
		CcpJsonRepresentation count = countWithMainRecords.put(JnEntityRecordsCount.Fields.twinRecords, twinRecords);
		JnEntityRecordsCount.ENTITY.save(count);
	}

	/**
	 * Makes the last writes of the index visible to the searches and counts its documents.
	 * @param entityName the name of the index
	 * @return how many documents it has
	 */
	private static long countRecords(String entityName) {
		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		dbRequester.refresh(entityName);
		long total = CcpQueryOptions.INSTANCE
				.matchAll()
				.selectFrom(entityName)
				.total();
		return total;
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
