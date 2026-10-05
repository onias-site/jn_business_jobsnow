package com.jn.business.messages;

import java.util.List;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** What happens when a message can not be sent (or is refused by a "do not send" rule). */
public enum JnMessageSenderExceptionHandler implements Function<Throwable, CcpJsonRepresentation> {
	/** Rethrows the failure. */
	THROWS{

		/**
		 * Rethrows the failure.
		 * @param e the failure
		 * @return never returns
		 * @throws JnErrorMessageSenderFailed always
		 */
		public CcpJsonRepresentation apply(Throwable e) {
			JnErrorMessageSenderFailed jnErrorMessageSenderFailed = new JnErrorMessageSenderFailed(e);
			throw jnErrorMessageSenderFailed;
		}

	},
	/** Records the failure as a warning in {@code jn_jobsnow_warning} and goes on. */
	LENIENT{

		/**
		 * Records the failure as a warning.
		 * @param e the failure
		 * @return the warning
		 */
		public CcpJsonRepresentation apply(Throwable e) {
			CcpJsonRepresentation errorDetails = getWarning(e);
			JnEntityJobsnowWarning.ENTITY.save(errorDetails);
			//ATTENTION: IT USED TO RETURN THE BUSINESS JSON
			return errorDetails;
		}

	},
	/** Records the failure as a warning in {@code jn_jobsnow_warning}, prints its stack trace and goes on. */
	LOG{

		/**
		 * Records the failure as a warning and prints its stack trace.
		 * @param e the failure
		 * @return the warning
		 */
		public CcpJsonRepresentation apply(Throwable e) {
			CcpJsonRepresentation errorDetails = getWarning(e);
			JnEntityJobsnowWarning.ENTITY.save(errorDetails);
			e.printStackTrace();
			//ATTENTION: IT USED TO RETURN THE BUSINESS JSON
			return errorDetails;
		}

	}

	;

	/**
	 * Translates the exception into the {@code JnEntityJobsnowWarning} format. The json that
	 * {@code CcpJsonRepresentation} builds from an exception carries {@code stackTrace} as a list and
	 * {@code cause} as empty text when there is no cause; the warning stores both as text and rejects
	 * empty text. Saving the raw json made the warning validation blow up — the {@code LENIENT}
	 * and {@code LOG} handlers could never record anything.
	 */
	private static CcpJsonRepresentation getWarning(Throwable e) {
		CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);

		String type = errorDetails.getAsString(JnJsonCommonsFields.type);
		String message = errorDetails.getAsString(JnJsonCommonsFields.message);
		List<String> stackTraceLines = errorDetails.getAsStringList(JnJsonCommonsFields.stackTrace);
		String stackTrace = String.join("\n", stackTraceLines);
		String cause = errorDetails.getAsString(JnJsonCommonsFields.cause);

		boolean hasNoMessage = message.trim().isEmpty();
		String messageOrType = hasNoMessage ? type : message;

		CcpJsonRepresentation warning = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.type, type)
				.put(JnJsonCommonsFields.message, messageOrType)
				.put(JnJsonCommonsFields.stackTrace, stackTrace);

		boolean hasNoCause = cause.trim().isEmpty();

		if(hasNoCause) {
			return warning;
		}

		CcpJsonRepresentation withCause = warning.put(JnJsonCommonsFields.cause, cause);
		return withCause;
	}

	/**
	 * Exception thrown by the {@code THROWS} policy to propagate to the caller the failure that occurred while sending the message.
	 */
	@SuppressWarnings("serial")
	public static class JnErrorMessageSenderFailed extends RuntimeException {
		/**
		 * Chains the original exception that occurred while sending the message.
		 * @param cause the original exception
		 */
		private JnErrorMessageSenderFailed(Throwable cause) {
			super("It was not possible to send the message", cause);
		}
	}

}
