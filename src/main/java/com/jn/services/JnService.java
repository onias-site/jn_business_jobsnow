package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.service.CcpService;

/**
 * Base of the services. By convention each item of the service enum has a top-level type with the same name in the same
 * package (usually declared as a secondary type in the service file, see {@code ValidateLogin} in
 * {@code JnServiceLogin.java}) holding its input rules. A nested type does not work: {@code Class.forName} would look
 * for {@code package.ServiceName$ItemName}, not {@code package.ItemName}, and fail with
 * {@link JnErrorServiceValidationClassNotFound}.
 */
public interface JnService extends CcpService { 
	/**
	 * Loads the input rules class named after the item.
	 * @return the class
	 * @throws JnErrorServiceValidationClassNotFound when there is no such class
	 */
	default Class<?> getJsonValidationClass() {
		
		Class<?> forName;
		try {
			forName = Class.forName(this.getClass().getPackageName() + "." + this.name());
		} catch (ClassNotFoundException e) {
			throw new JnErrorServiceValidationClassNotFound(e);
		}
		return forName;
	}

	/**
	 * Runs the service (see {@code CcpService}).
	 * @param json the request
	 * @return the response
	 */
	default CcpJsonRepresentation execute(CcpJsonRepresentation json) {
		CcpJsonRepresentation execute = CcpService.super.execute(json);
		return execute;
	}
	

}
