package com.jn.business.errors;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.json.fields.validation.JnApiOrigins;

/**
 * Records an unhandled error of an API in {@code jn_jobsnow_error} with the API it came from, which chooses the topic of
 * the notice in the group of the stack traces.
 */
public class JnBusinessSaveApiError implements CcpBusiness {

	/** The API that records the errors. */
	private final JnApiOrigins origin;

	/**
	 * Keeps the API that records the errors.
	 * @param origin the API
	 */
	public JnBusinessSaveApiError(JnApiOrigins origin) {
		this.origin = origin;
	}

	/**
	 * Saves the error.
	 * @param json the error
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String originName = this.origin.name();
		CcpJsonRepresentation errorWithOrigin = json.put(JnEntityJobsnowError.Fields.origin, originName);
		JnEntityJobsnowError.ENTITY.save(errorWithOrigin);
		return json;
	}
}
