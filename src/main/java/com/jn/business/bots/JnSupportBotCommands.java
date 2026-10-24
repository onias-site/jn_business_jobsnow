package com.jn.business.bots;

/**
 * The commands of the support bot that belong to jn: {@code allowCommandToUser} (stops ignoring a user) and
 * {@code ignoredUsers} (goes through the users ignored by the support). The ignoring is shared by every cost center that
 * receives requests of the users (vis and js), so it lives in jn ({@code JnEntityCommandNotAllowedToUser}), and these
 * commands with it. jn seeds their configuration in the bot entities (command, steps, names and texts) and the bot runs
 * them by the class names found there. Until 2026-10-23 they were items of {@code VisSupportBotCommands}, in vis.
 */
public enum JnSupportBotCommands {

	/** Stops ignoring a user. */
	allowCommandToUser,
	/** Goes through the users ignored by the support, to stop ignoring them. */
	ignoredUsers
	;
}
