package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault.JsonFieldNames;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Prepara o JSON do token de login antes da gravação na entidade principal. Traz o conteúdo do
 * json interno {@code request} para o nível raiz, aplica o transformador de {@code token} (que
 * gera o token aleatório e guarda o original em {@code originalToken}) e replica os valores
 * originais: o {@code originalEmail} para {@code email} e {@code chatId}, e o
 * {@code originalToken} para {@code token}.
 */
public class JnBusinessPrepareLoginTokenBeforeSave implements CcpBusiness {

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpJsonRepresentation request = json.getInnerJson(JnJsonCommonsFields.request);
		CcpJsonRepresentation mergeWithAnotherJson = request.mergeWithAnotherJson(json);
		CcpJsonRepresentation transformedJson2 = mergeWithAnotherJson
				.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token);
		CcpJsonRepresentation duplicateValueFromField = transformedJson2
				.duplicateValueFromField(JsonFieldNames.originalEmail, JnJsonCommonsFields.email,
						JnJsonInstantMessengerFields.chatId);
		CcpJsonRepresentation transformedJson = duplicateValueFromField
				.duplicateValueFromField(JnJsonCommonsFields.originalToken, JnJsonTransformersFieldsEntityDefault.token)
				;
		return transformedJson;
	}

}
