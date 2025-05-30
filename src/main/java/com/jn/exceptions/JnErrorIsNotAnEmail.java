package com.jn.exceptions;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.exceptions.db.utils.CcpEntityJsonTransformerError;

@SuppressWarnings("serial")
public class JnErrorIsNotAnEmail extends CcpEntityJsonTransformerError {
	public JnErrorIsNotAnEmail(String content, CcpJsonRepresentation json) {
		super("The text '" + content + "' is not a valid email in the json " + json);
	}
}
