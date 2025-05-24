package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;

public class AndWithTheEntityToBlockMessageResend {
	final WithTheTemplateId withTemplateId;
	
	final CcpEntity entityToSave;

	AndWithTheEntityToBlockMessageResend(WithTheTemplateId withTemplateId, CcpEntity entityToSave) {
		this.withTemplateId = withTemplateId;
		this.entityToSave = entityToSave;
	}
	
	public AndWithTheJsonValues andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues) {
		return new AndWithTheJsonValues(this, jsonValues);
	}
}
