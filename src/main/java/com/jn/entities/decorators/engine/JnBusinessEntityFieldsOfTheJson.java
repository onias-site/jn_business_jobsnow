package com.jn.entities.decorators.engine;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;

/**
 * Business of {@link JnVersionableEntity} run when the record is not in the database yet: answers the fields of the
 * entity present in the given json, after the entity handled it. Its own input is ignored.
 */
class JnBusinessEntityFieldsOfTheJson implements CcpBusiness {

	/** The details of the entity. */
	private final CcpEntityMetaData entityDetails;

	/** The json that holds the fields. */
	private final CcpJsonRepresentation json;

	/**
	 * Keeps the entity and the json.
	 * @param entityDetails the details of the entity
	 * @param json the json that holds the fields
	 */
	JnBusinessEntityFieldsOfTheJson(CcpEntityMetaData entityDetails, CcpJsonRepresentation json) {
		this.entityDetails = entityDetails;
		this.json = json;
	}

	/**
	 * Answers the fields of the entity present in the json.
	 * @param ignored the input, not used
	 * @return the fields of the entity present in the json
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation ignored) {
		CcpJsonRepresentation handledJson = this.entityDetails.entity.getHandledJson(this.json);
		CcpJsonRepresentation onlyExistingFields = this.entityDetails.getOnlyExistingFields(handledJson);
		return onlyExistingFields;
	}
}
