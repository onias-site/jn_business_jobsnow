package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityJsonTransformerError;

@SuppressWarnings("serial")
public class JnErrorIsNotAnEmail extends CcpEntityJsonTransformerError {
	public JnErrorIsNotAnEmail(String content, CcpJsonRepresentation json) {
		super("The text '" + content + "' is not a valid email in the json " + json);
	}
}
