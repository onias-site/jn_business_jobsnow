package com.jn.exceptions;

@SuppressWarnings("serial")
public class JnErrorSupportLanguageIsMissing extends RuntimeException {
	public JnErrorSupportLanguageIsMissing() {
		super("It is missing the configuration 'supportLanguage'");
	}
}
