package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class AndWithTheJsonValues {

	final AndWithTheEntityToBlockMessageResend andWithEntityToSave;
	
	final CcpJsonRepresentation jsonValues;

	AndWithTheJsonValues(AndWithTheEntityToBlockMessageResend andWithEntityToSave, CcpJsonRepresentation jsonValues) {
		this.andWithEntityToSave = andWithEntityToSave;
		this.jsonValues = jsonValues;
	}
	
	public AndWithTheSupportLanguage andWithTheSupportLanguage(String supportLanguage) {
		return new AndWithTheSupportLanguage(this, supportLanguage);
	}
	
}
