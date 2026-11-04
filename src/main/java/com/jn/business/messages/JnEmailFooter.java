package com.jn.business.messages;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * The footer of the e-mails sent to people outside the support team, one item per language: the name of JobsNow and the
 * link to its privacy policy. The physical mailing address is left out on purpose. The HTML uses single quotes only, so
 * that it also fits inside the templates written as JSON text.
 */
public enum JnEmailFooter implements CcpJsonFieldName {
	/** The footer in Portuguese. */
	portuguese { public String getValue() { return "<hr/><p style='font-size: 12px; color: #777777;'>JobsNow · "
			+ "<a href='https://onias.site/privacidade' target='_blank'>Política de privacidade</a></p>"; } },
	/** The footer in English. */
	english { public String getValue() { return "<hr/><p style='font-size: 12px; color: #777777;'>JobsNow · "
			+ "<a href='https://onias.site/privacidade' target='_blank'>Privacy policy</a></p>"; } },
}
