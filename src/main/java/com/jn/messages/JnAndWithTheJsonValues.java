package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class JnAndWithTheJsonValues {

	final JnWithTheTemplateId andWithTheTemplateId;

	final CcpJsonRepresentation jsonValues;

	JnAndWithTheJsonValues(JnWithTheTemplateId andWithTheTemplateId, CcpJsonRepresentation jsonValues) {
		this.andWithTheTemplateId = andWithTheTemplateId;
		this.jsonValues          = jsonValues;
	}

	public JnAndWithTheSupportLanguage andWithTheSupportLanguage(String supportLanguage) {
		JnAndWithTheSupportLanguage jnAndWithTheSupportLanguage = new JnAndWithTheSupportLanguage(this, supportLanguage);
		return jnAndWithTheSupportLanguage;
	}
}
