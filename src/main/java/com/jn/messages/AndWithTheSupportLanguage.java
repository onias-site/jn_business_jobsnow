package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

public class AndWithTheSupportLanguage {

	final AndWithTheJsonValues andWithJsonValues;
	
	final String supportLanguage;

	AndWithTheSupportLanguage(AndWithTheJsonValues andWithJsonValues, String supportLanguage) {
		this.andWithJsonValues = andWithJsonValues;
		this.supportLanguage = supportLanguage;
	}
	
	public CcpJsonRepresentation sendAllMessages() {
		CcpJsonRepresentation executeAllSteps = this.andWithJsonValues.andWithEntityToSave.withTemplateId.soExecuteAllAddedSteps.getMessage.
		executeAllSteps(this.andWithJsonValues.andWithEntityToSave.withTemplateId.templateId, 
				this.andWithJsonValues.andWithEntityToSave.entityToSave, this.andWithJsonValues.jsonValues, supportLanguage);
		return executeAllSteps;
	}
}
