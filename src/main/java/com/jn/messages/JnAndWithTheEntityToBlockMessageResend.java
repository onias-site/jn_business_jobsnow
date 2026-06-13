package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * Etapa do builder fluent que recebe a entidade usada para bloquear reenvio de mensagens
 * (deduplicação). Avança para {@code JnAndWithTheJsonValues} ao fornecer os valores do JSON.
 */
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
