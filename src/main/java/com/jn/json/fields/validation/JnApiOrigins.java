package com.jn.json.fields.validation;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * The APIs that record unhandled errors: each one is the {@code origin} of its errors in {@code JnEntityJobsnowError} and
 * the key of its topic in the group of the stack traces.
 */
public enum JnApiOrigins implements CcpJsonFieldName {
	/** The REST API of the vis cost center. */
	vis,
	/** The REST API of the js cost center. */
	js,
}
