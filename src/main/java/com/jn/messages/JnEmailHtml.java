package com.jn.messages;

/**
 * Text typed by a person (the justification of a candidate, the answer of the support, a ticket of the "contact us")
 * that goes into the HTML body of an email: escaped, so that a {@code <} typed by them shows up as text, and with the
 * line breaks kept. Since 2026-10-23 in jn, shared by vis and js; it was {@code VisEmailHtml.escape}.
 */
public final class JnEmailHtml {

	/** Utility class; not instantiable. */
	private JnEmailHtml() {}

	/**
	 * Escapes the text for an HTML email body and turns its line breaks into {@code <br>}.
	 * @param text the typed text
	 * @return the escaped text
	 */
	public static String escape(String text) {
		String withoutAmpersand = text.replace("&", "&amp;");
		String withoutLessThan = withoutAmpersand.replace("<", "&lt;");
		String withoutGreaterThan = withoutLessThan.replace(">", "&gt;");
		// the body is still a template: a {email} typed by someone would be filled by the sending
		String withoutOpeningBrace = withoutGreaterThan.replace("{", "&#123;");
		String withoutClosingBrace = withoutOpeningBrace.replace("}", "&#125;");
		String withLineBreaks = withoutClosingBrace.replace("\n", "<br>");
		return withLineBreaks;
	}
}
