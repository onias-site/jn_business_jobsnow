package com.jn.json.fields.validation;

/**
 * Support bot commands that review a request made by the user. The {@code commandName} of
 * {@code JnEntityCommandNotAllowedToUser} records in which of them the user was ignored (the ignoring itself is
 * global). Each name is the same as the one of the support bot command, whatever the cost center of the command.
 */
public enum JnUserRequestCommands {
	/** The command that reviews a skill hierarchy fix request (vis). */
	fixSkillHierarchy,
	/** The command that reviews a skill suggestion (vis). */
	reviewSkillSuggestion,
	/** The command that answers a ticket of the "contact us" (js). */
	answerContactUs
}
