package com.jn.utils;

import com.ccp.decorators.CcpJsonFieldName;

public enum Fields implements CcpJsonFieldName{
	databaseAddress{
		public String getValue() {
			return "database.address";
		}
	},
	databaseSecret{
		public String getValue() {
			return "database.secret";
		}
	},
	maxAttempts,
	supportLanguage,
	urlEmailKey,
	urlInstantMessengerKey,
	tokenEmailKey,
	tokenInstantMessengerKey,
	localEnvironment,
	languages,
	systems
	
}
