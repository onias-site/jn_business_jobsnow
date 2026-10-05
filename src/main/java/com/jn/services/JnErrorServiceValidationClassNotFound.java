package com.jn.services;


/** Raised when a service item has no input rules class with its name (see {@link JnService}). */
@SuppressWarnings("serial")
public class JnErrorServiceValidationClassNotFound extends RuntimeException {
	/**
	 * Wraps the lookup failure.
	 * @param cause the {@code ClassNotFoundException}
	 */
	JnErrorServiceValidationClassNotFound(Throwable cause) {
		super(cause);
	}
}
