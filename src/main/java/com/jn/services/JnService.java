package com.jn.services;

import com.ccp.service.CcpService;

public interface JnService extends CcpService {
	default Class<?> getJsonValidationClass() {
		Class<?> forName;
		try {
			forName = Class.forName(this.getClass().getPackageName() + "." + this.name());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return forName;
	}

}
