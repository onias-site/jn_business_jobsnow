package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Etapa do builder fluent que carrega os valores do JSON da mensagem. Avança para
 * {@code JnAndWithTheSupportLanguage} ao fornecer o idioma de suporte.
 */
public class JnAndWithTheJsonValues {

	final JnAndWithTheEntityToBlockMessageResend andWithEntityToSave;
	
	final CcpJsonRepresentation jsonValues;

	JnAndWithTheJsonValues(JnAndWithTheEntityToBlockMessageResend andWithEntityToSave, CcpJsonRepresentation jsonValues) {
		this.andWithEntityToSave = andWithEntityToSave;
		this.jsonValues = jsonValues;
	}
	
	public JnAndWithTheSupportLanguage andWithTheSupportLanguage(String supportLanguage) {
		return new JnAndWithTheSupportLanguage(this, supportLanguage);
	}
	
}
