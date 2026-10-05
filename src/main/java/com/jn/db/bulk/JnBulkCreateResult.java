package com.jn.db.bulk;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Outcome of one json sent to {@link JnExecuteBulkOperation#executeCreateBulk}: the json itself and whether
 * the bulk created it ({@code true}, status 201) or left it out because it already existed or failed
 * ({@code false}, status 409 or any other error).
 */
public class JnBulkCreateResult {

	/** The JSON sent. */
	public final CcpJsonRepresentation json;

	/** Whether every record derived from the JSON was created. */
	public final boolean created;

	/**
	 * Pairs the JSON with its outcome.
	 * @param json the JSON sent
	 * @param created whether it was created
	 */
	JnBulkCreateResult(CcpJsonRepresentation json, boolean created) {
		this.json = json;
		this.created = created;
	}
}
