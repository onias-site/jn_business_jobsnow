package com.jn.exceptions;

/**
 * Lançada por {@code JnBusinessNotifySupport} quando a propriedade {@code supportLanguage} não está
 * configurada no {@code application_properties}. Indica erro de configuração do ambiente.
 */
@SuppressWarnings("serial")
public class JnErrorSupportLanguageIsMissing extends RuntimeException {
	public JnErrorSupportLanguageIsMissing() {
		super("It is missing the configuration 'supportLanguage'");
	}
}
