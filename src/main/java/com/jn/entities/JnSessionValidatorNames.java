package com.jn.entities;

/**
 * The names of the validators of session kept in {@link JnEntitySessionValidator}.
 */
public enum JnSessionValidatorNames {
	/** The session of the user who logged in. */
	userSession,
	/**
	 * The login token e-mailed to the user, typed outside the login screen (the command {@code /receiveMessages} of the
	 * user bot, since 2026-11-12): the validator answers whether the token is right, without counting attempts.
	 */
	loginToken,
	;
}
