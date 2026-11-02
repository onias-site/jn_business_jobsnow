package com.jn.business.email.suppression;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessageSenderExceptionHandler;

/**
 * Runs {@link JnBusinessLoadEmailSuppressions} inside a process that has other work to do: a failure of the provider (an
 * invalid key, a rate limit) becomes a record in {@code jn_jobsnow_warning} and must not cost the rest of the process. The
 * next load reads the same period again.
 * <p>
 * Input: the one of the load. Output: the counters of the load, or the warning.
 */
public class JnBusinessLoadEmailSuppressionsOrSaveAWarning implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JnBusinessLoadEmailSuppressionsOrSaveAWarning INSTANCE = new JnBusinessLoadEmailSuppressionsOrSaveAWarning();

	private JnBusinessLoadEmailSuppressionsOrSaveAWarning() {}

	/**
	 * Runs the load and keeps a failure as a warning.
	 * @param json the input of the load
	 * @return the counters of the load, or the warning
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		try {
			CcpJsonRepresentation loaded = JnBusinessLoadEmailSuppressions.INSTANCE.execute(json);
			CcpJsonRepresentation counters = loaded.getJsonPiece(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions, JnBusinessLoadEmailSuppressions.Fields.ignoredSuppressions);
			return counters;
		} catch (RuntimeException e) {
			CcpJsonRepresentation warning = JnMessageSenderExceptionHandler.LENIENT.apply(e);
			return warning;
		}
	}
}
