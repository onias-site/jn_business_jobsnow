package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Transforms the {@code message} field of {@code JnEntityInstantMessengerMessageSent} into its SHA-1 hash,
 * keeping the original in {@code originalMessage}. It allows message deduplication — the same
 * message to the same recipient within the same hour is not sent again.
 */
public class JnJsonTransformersFieldEntityMessageHash implements CcpJsonTransformersDefaultEntityField {
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		String originalMessage = json.getAsString(JnJsonCommonsFields.message);
		CcpStringDecorator messageDecorator = json.getAsStringDecorator(JnJsonCommonsFields.message);
		var messageHashDecorator = messageDecorator.hash();

		String hashedMessage = messageHashDecorator.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation jsonWithHashedMessage = json
				.put(JnJsonCommonsFields.message, hashedMessage);

				CcpJsonRepresentation jsonWithOriginalMessage = jsonWithHashedMessage
				.put(JsonFieldNames.originalMessage, originalMessage)
				;
		return jsonWithOriginalMessage;
	}

	public boolean canBePrimaryKey() {
		return true;
	}

	public String name() {
		String messageHashName = JsonFieldNames.messageHash.name();
		return messageHashName;
	}
}
