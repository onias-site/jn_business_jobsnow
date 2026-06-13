package com.jn.utils;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

/**
 * Define os idiomas suportados pela plataforma JobsNow ({@code portuguese}, {@code english},
 * {@code spanish}). Usado como valor do campo {@code language} em entidades de template de email
 * e mensagem instantânea.
 */
public enum JnLanguage implements CcpJsonFieldName{

	portuguese,
	english,
	spanish
}
