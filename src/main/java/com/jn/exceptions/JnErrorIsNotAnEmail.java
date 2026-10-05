package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityJsonTransformerError;

/**
 * Raised by the {@code email} transformer of {@code JnJsonTransformersFieldsEntityDefault} when the value of the field
 * is not a valid e-mail address.
 */
@SuppressWarnings("serial")
public class JnErrorIsNotAnEmail extends CcpEntityJsonTransformerError {
	/**
	 * Builds the error with the invalid value and the JSON.
	 * @param content the invalid value
	 * @param json the record
	 */
	public JnErrorIsNotAnEmail(String content, CcpJsonRepresentation json) {
		super("The text '" + content + "' is not a valid email in the json " + json);
	}
}
