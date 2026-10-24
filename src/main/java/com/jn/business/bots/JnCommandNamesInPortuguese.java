package com.jn.business.bots;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * How each command of {@link JnSupportBotCommands} is typed in Portuguese (without the {@code /}): the item is the
 * canonical name of the command and {@link #getValue()} its name in Portuguese. Seeded in {@code JnEntityBotCommandName};
 * the canonical name stays valid in every language, and in English it is the name itself.
 */
public enum JnCommandNamesInPortuguese implements CcpJsonFieldName{
	/** {@code /permitirComandoAoUsuario}. */
	allowCommandToUser { public String getValue() { return "permitirComandoAoUsuario"; } },
	/** {@code /listarUsuariosIgnorados}. */
	ignoredUsers { public String getValue() { return "listarUsuariosIgnorados"; } },
	;
}
