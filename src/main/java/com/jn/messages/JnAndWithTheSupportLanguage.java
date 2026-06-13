package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Última etapa do builder fluent. Recebe o idioma de suporte e dispara a execução de todos os
 * steps configurados via {@code sendAllMessages()}, que é o método terminal da cadeia fluent.
 */
public class JnAndWithTheSupportLanguage {

	final JnAndWithTheJsonValues andWithJsonValues;
	
	final String supportLanguage;

	JnAndWithTheSupportLanguage(JnAndWithTheJsonValues andWithJsonValues, String supportLanguage) {
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
