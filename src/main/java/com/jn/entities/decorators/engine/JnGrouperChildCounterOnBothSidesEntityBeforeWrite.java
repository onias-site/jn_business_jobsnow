package com.jn.entities.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChildCounter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;

/**
 * Decorator of {@code @CcpEntityGrouperChildCounter} (given as its {@code beforeDecoratorClass}) that counts the record
 * as {@link JnGrouperChildCounterEntityBeforeWrite} does, but finds its previous write on either side of a twin entity:
 * the record stored on the main side or, when it is only on the twin side (deactivated), the one stored there. So a
 * record brought back from the twin is not counted again in the parents it was already in. Made for the views of the
 * resumes ({@code vis_viewed_resume}): in the screen "Quem me viu", a recruiter who views again, in the same month, a
 * resume whose view went to the twin is the same recruiter, counted once per month; in the chart of the companies that
 * view the most resumes (screen Curiosities), the same view is not counted twice (until 2026-11-14 both counted again).
 */
public class JnGrouperChildCounterOnBothSidesEntityBeforeWrite extends JnGrouperChildCounterEntityBeforeWrite {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the counting configured for the entity
	 */
	public JnGrouperChildCounterOnBothSidesEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChildCounter annotation) {
		super(entity, annotation);
	}

	/**
	 * The record as stored on the main side or, when it is not there and the entity has a twin, on the twin side.
	 * @param json the record (at least its key, not transformed yet)
	 * @return the stored record, empty when it is in neither side
	 */
	protected CcpJsonRepresentation getStoredRecord(CcpJsonRepresentation json) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpJsonRepresentation storedOnTheMainSide = getStored(mainEntity, json);
		boolean onTheMainSide = false == storedOnTheMainSide.isEmpty();

		if(onTheMainSide) {
			return storedOnTheMainSide;
		}

		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		CcpEntityTwin twin = mainDetails.configurationClass.getAnnotation(CcpEntityTwin.class);
		boolean hasNoTwin = twin == null;

		if(hasNoTwin) {
			return storedOnTheMainSide;
		}

		CcpEntity twinEntity = mainEntity.getTwinEntity();
		CcpJsonRepresentation storedOnTheTwinSide = getStored(twinEntity, json);
		return storedOnTheTwinSide;
	}

	/**
	 * The record as stored on one side of the entity, read by id straight from the database (no cache).
	 * @param sideEntity the side read
	 * @param json the record (at least its key, not transformed yet)
	 * @return the stored record, empty when it is not there
	 */
	private static CcpJsonRepresentation getStored(CcpEntity sideEntity, CcpJsonRepresentation json) {
		CcpJsonRepresentation jsonWithHandledPrimaryKey = sideEntity.getJsonWithHandledPrimaryKey(json);
		CcpEntityMetaData sideDetails = sideEntity.getEntityMetaData();
		// the transformers also keep the original values: only the transformed ones calculate the id
		CcpJsonRepresentation recordKey = sideDetails.getOnlyExistingFields(jsonWithHandledPrimaryKey);
		CcpJsonRepresentation storedRecord = sideDetails.getOneByIdOrHandleItIfThisIdWasNotFound(recordKey, notFound -> CcpOtherConstants.EMPTY_JSON);
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
