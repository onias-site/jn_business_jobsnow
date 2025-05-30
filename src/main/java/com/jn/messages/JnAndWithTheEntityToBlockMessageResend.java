package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;

public class JnAndWithTheEntityToBlockMessageResend {
	final JnWithTheTemplateId withTemplateId;
	
	final CcpEntity entityToSave;

	JnAndWithTheEntityToBlockMessageResend(JnWithTheTemplateId withTemplateId, CcpEntity entityToSave) {
		this.withTemplateId = withTemplateId;
		this.entityToSave = entityToSave;
	}
	
	public JnAndWithTheJsonValues andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues) {
		return new JnAndWithTheJsonValues(this, jsonValues);
	}
}
