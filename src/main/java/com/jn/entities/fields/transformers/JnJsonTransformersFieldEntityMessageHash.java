package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;

/**
 * Transforma o campo {@code message} de {@code JnEntityInstantMessengerMessageSent} em seu hash SHA-1,
 * preservando o original em {@code originalMessage}. Permite deduplicação de mensagens — a mesma
 * mensagem para o mesmo destinatário na mesma hora não é reenviada.
 */
public class JnJsonTransformersFieldEntityMessageHash implements CcpJsonTransformersDefaultEntityField {
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		String originalToken = json.getAsString(JnEntityInstantMessengerMessageSent.Fields.message);
		
		String token = json.getAsStringDecorator(JnEntityInstantMessengerMessageSent.Fields.message).hash().asString(CcpHashAlgorithm.SHA1);
		
		CcpJsonRepresentation put = json
				.put(JnEntityInstantMessengerMessageSent.Fields.message, token)
				.put(JsonFieldNames.originalMessage, originalToken)
				;
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		return JsonFieldNames.messageHash.name();
	}
}
