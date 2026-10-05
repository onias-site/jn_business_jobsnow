package com.jn.utils;

import com.ccp.decorators.CcpJsonFieldName;

/** Keys of the system properties read by {@link JnSystemProperties}. */
public enum Fields implements CcpJsonFieldName{
	/** The {@code database.address} property: the address of the database. */
	databaseAddress{
		/** @return the dotted property name */
		public String getValue() {
			return "database.address";
		}
	},
	/** The {@code database.secret} property: the credential of the database. */
	databaseSecret{
		/** @return the dotted property name */
		public String getValue() {
			return "database.secret";
		}
	},
	/** The {@code maxAttempts} property: how many wrong secrets lock a password or token. */
	maxAttempts,
	/** The {@code supportLanguage} property: the language of the messages sent to the support team. */
	supportLanguage,
	/** The {@code urlEmailKey} property: the URL of the e-mail API. */
	urlEmailKey,
	/** The {@code urlInstantMessengerKey} property: the URL of the instant messenger API. */
	urlInstantMessengerKey,
	/** The {@code tokenEmailKey} property: the credential of the e-mail API. */
	tokenEmailKey,
	/** The {@code tokenInstantMessengerKey} property: the credential of the instant messenger API. */
	tokenInstantMessengerKey,
	/** The {@code localEnvironment} property: whether the system runs on a developer machine. */
	localEnvironment,
	/** The {@code languages} property. */
	languages,
	/** The {@code systems} property: the systems served by this installation. */
	systems
	
}
