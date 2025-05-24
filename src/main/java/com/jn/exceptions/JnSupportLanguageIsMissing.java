package com.jn.exceptions;

@SuppressWarnings("serial")
public class JnSupportLanguageIsMissing extends RuntimeException {
	public JnSupportLanguageIsMissing() {
		super("It is missing the configuration 'supportLanguage'");
	}
}
