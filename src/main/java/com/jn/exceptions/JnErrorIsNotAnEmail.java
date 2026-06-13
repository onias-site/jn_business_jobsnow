package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityJsonTransformerError;

/**
 * Lançada pelo transformador {@code email} de {@code JnJsonTransformersFieldsEntityDefault} quando
 * o valor do campo não é um endereço de email válido. Inclui o valor inválido e o JSON de contexto.
 */
@SuppressWarnings("serial")
public class JnErrorIsNotAnEmail extends CcpEntityJsonTransformerError {
	public JnErrorIsNotAnEmail(String content, CcpJsonRepresentation json) {
		super("The text '" + content + "' is not a valid email in the json " + json);
	}
}
