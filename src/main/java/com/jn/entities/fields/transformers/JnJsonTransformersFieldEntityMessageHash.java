package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Transforma o campo {@code message} de {@code JnEntityInstantMessengerMessageSent} em seu hash SHA-1,
 * preservando o original em {@code originalMessage}. Permite deduplicação de mensagens — a mesma
 * mensagem para o mesmo destinatário na mesma hora não é reenviada.
 */
public class JnJsonTransformersFieldEntityMessageHash implements CcpJsonTransformersDefaultEntityField {
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		String originalToken = json.getAsString(JnJsonCommonsFields.message);
		CcpStringDecorator asStringDecorator = json.getAsStringDecorator(JnJsonCommonsFields.message);
		var asStringDecoratorHash = asStringDecorator.hash();

		String token = asStringDecoratorHash.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation put2 = json
				.put(JnJsonCommonsFields.message, token);

				CcpJsonRepresentation put = put2
				.put(JsonFieldNames.originalMessage, originalToken)
				;
		return put;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		String messageHashName = JsonFieldNames.messageHash.name();
		return messageHashName;
	}
}
