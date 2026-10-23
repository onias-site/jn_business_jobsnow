package com.jn.business.session;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.jn.entities.JnEntitySessionValidator;
import com.jn.entities.JnSessionValidatorNames;

/**
 * Validates the session of the user with the business seeded in {@link JnEntitySessionValidator}, run by its class
 * name: the APIs protect their routes with it without importing the cost center of the login.
 */
public class JnBusinessValidateSession implements CcpBusiness {

	/** The validator of the session of the user who logged in. */
	public static final JnBusinessValidateSession INSTANCE = new JnBusinessValidateSession();

	private JnBusinessValidateSession() {}

	/**
	 * Runs the seeded validator over the request.
	 * @param json the request, with the session values
	 * @return what the seeded validator answers
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation key = JnEntitySessionValidator.getKey(JnSessionValidatorNames.userSession);
		CcpJsonRepresentation validator = JnEntitySessionValidator.ENTITY.getOneById(key);
		String businessName = validator.getAsString(JnEntitySessionValidator.Fields.business);
		CcpStringDecorator businessNameDecorator = new CcpStringDecorator(businessName);
		CcpReflectionConstructorDecorator reflection = businessNameDecorator.reflection();
		CcpBusiness business = reflection.newInstance();
		CcpJsonRepresentation validated = business.execute(json);
		return validated;
	}
}
