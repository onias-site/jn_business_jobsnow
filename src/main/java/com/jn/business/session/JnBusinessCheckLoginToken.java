package com.jn.business.session;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntitySessionValidator;
import com.jn.entities.JnSessionValidatorNames;

/**
 * Tells whether a login token typed by the user is right (since 2026-11-12), with the business that the cost center of
 * the login seeds in {@link JnEntitySessionValidator} under {@code loginToken}, run by its class name: the user bot
 * checks the token without importing that cost center. The seeded business counts no attempts: whoever asks keeps its
 * own count.
 */
public class JnBusinessCheckLoginToken implements CcpBusiness {

	/** The single instance: the class keeps no state. */
	public static final JnBusinessCheckLoginToken INSTANCE = new JnBusinessCheckLoginToken();

	private JnBusinessCheckLoginToken() {}

	/** The answer of the seeded business. */
	public enum Fields implements CcpJsonFieldName {
		/** {@code true} when the typed token is the one e-mailed to the user. */
		rightToken,
	}

	/**
	 * Runs the seeded business.
	 * @param json {@code email} and the typed {@code token}
	 * @return the json with {@code rightToken}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpBusiness business = JnEntitySessionValidator.getValidator(JnSessionValidatorNames.loginToken);
		CcpJsonRepresentation checked = business.execute(json);
		return checked;
	}
}
