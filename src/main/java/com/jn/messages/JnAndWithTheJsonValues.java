package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class JnAndWithTheJsonValues {

	final JnAndWithTheEntityToBlockMessageResend andWithEntityToSave;

	final CcpJsonRepresentation jsonValues;

	JnAndWithTheJsonValues(JnAndWithTheEntityToBlockMessageResend andWithEntityToSave, CcpJsonRepresentation jsonValues) {
		this.andWithEntityToSave = andWithEntityToSave;
		this.jsonValues          = jsonValues;
	}

	public JnAndWithTheSupportLanguage andWithTheSupportLanguage(String supportLanguage) {
		JnAndWithTheSupportLanguage jnAndWithTheSupportLanguage = new JnAndWithTheSupportLanguage(this, supportLanguage);
		return jnAndWithTheSupportLanguage;
	}
}
