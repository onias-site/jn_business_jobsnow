package com.jn.business.bots.stack.trace;

import com.ccp.decorators.CcpJsonFieldName;

/** Fields of the {@code solveStackTrace} command and of its texts. */
public enum JnSupportStackTraceFields implements CcpJsonFieldName {
	/** The parameter of the command: the id of the error, in {@code jn_jobsnow_error}. */
	errorId,
	/** The options offered to the operator. */
	options
}
