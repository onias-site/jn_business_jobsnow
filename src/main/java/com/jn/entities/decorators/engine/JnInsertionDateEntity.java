package com.jn.entities.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Decorator of {@code @CcpEntityInsertionDate} (given as its {@code value}): every save writes, in
 * {@code insertionDate} ({@code dd/MM/yyyy HH:mm:ss}), the moment the record was inserted: the one stored on the main
 * side or, when the record is only on the twin side, the one stored there; the current moment when the record is in
 * neither side or was stored before this decorator existed. The value the input brings is never used.
 */
public class JnInsertionDateEntity extends CcpEntityDelegator {

	/** The format of {@code insertionDate}. */
	enum Constants implements CcpJsonFieldName{
		DATE_FORMAT {
			public String getValue() {
				return "dd/MM/yyyy HH:mm:ss";
			}
		}
	}

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 */
	public JnInsertionDateEntity(CcpEntity entity) {
		super(entity);
	}

	/**
	 * Saves the record with the moment of its insertion.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		String insertionDate = this.getInsertionDate(json);
		CcpJsonRepresentation jsonWithInsertionDate = json.put(JnJsonCommonsFields.insertionDate, insertionDate);
		boolean inserted = this.entity.save(jsonWithInsertionDate);
		return inserted;
	}

	/**
	 * The stored moment of the insertion of the record, or the current moment.
	 * @param json the record (at least its key, not transformed yet)
	 * @return the moment, as {@code dd/MM/yyyy HH:mm:ss}
	 */
	private String getInsertionDate(CcpJsonRepresentation json) {
		CcpEntity mainEntity = this.getMainEntity();
		CcpJsonRepresentation storedOnTheMainSide = getStored(mainEntity, json);
		boolean onTheMainSide = storedOnTheMainSide.containsField(JnJsonCommonsFields.insertionDate);

		if(onTheMainSide) {
			return storedOnTheMainSide.getAsString(JnJsonCommonsFields.insertionDate);
		}

		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		CcpEntityTwin twin = mainDetails.configurationClass.getAnnotation(CcpEntityTwin.class);
		boolean hasTwin = twin != null;
		CcpEntity twinEntity = hasTwin ? mainEntity.getTwinEntity() : mainEntity;
		CcpJsonRepresentation storedOnTheTwinSide = getStored(twinEntity, json);
		boolean onTheTwinSide = storedOnTheTwinSide.containsField(JnJsonCommonsFields.insertionDate);

		if(onTheTwinSide) {
			return storedOnTheTwinSide.getAsString(JnJsonCommonsFields.insertionDate);
		}

		String dateFormat = Constants.DATE_FORMAT.getValue();
		CcpTimeDecorator now = new CcpTimeDecorator();
		String currentDate = now.getFormattedDateTime(dateFormat);
		return currentDate;
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
