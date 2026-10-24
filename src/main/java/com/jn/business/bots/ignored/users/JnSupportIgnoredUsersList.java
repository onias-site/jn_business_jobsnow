package com.jn.business.bots.ignored.users;

import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.json.fields.validation.JnUserRequestCommands;

/**
 * The users ignored by the support ({@link JnEntityCommandNotAllowedToUser}) and the texts that show them, shared by
 * the steps of the {@code ignoredUsers} command.
 *
 * <p>The users are found by a query (a support bot command may run one: only the support team issues them), which
 * Elasticsearch answers from what was refreshed up to a second ago, so each user found is confirmed by its id, read
 * in real time: without that, a user the operator has just stopped ignoring would still be listed. The email of the
 * record is kept as hash, so the list takes the readable one from the {@code description} (written there since
 * 2026-10-11); a user ignored before that cannot be shown by email and is left out of the list. The request that led to
 the decision is shown by its summary ({@code request} of {@code JnJsonCommonsFields}), written by the
 cost center of the command that ignored the user. Also read by the commands that list the users ignored in one command
 ({@code /ignoredContactUs}, of js).
 */
public final class JnSupportIgnoredUsersList {

	/** Utility class; not instantiable. */
	private JnSupportIgnoredUsersList() {}

	/**
	 * The ignored users, ordered by email, each one with its readable {@code email}, the {@code commandName} in which
	 * the operator ignored them and the {@code description} of the request.
	 * @return the ignored users
	 */
	static List<CcpJsonRepresentation> getIgnoredUsers() {
		CcpQueryOptions requestWithEveryUser = CcpQueryOptions.INSTANCE.matchAll();
		List<CcpJsonRepresentation> ignoredUsers = getIgnoredUsers(requestWithEveryUser);
		return ignoredUsers;
	}

	/**
	 * The users ignored in one command, ordered by email, as {@link #getIgnoredUsers()} gives them.
	 * @param command the command in which the operator ignored them
	 * @return the ignored users
	 */
	public static List<CcpJsonRepresentation> getIgnoredUsers(JnUserRequestCommands command) {
		var requestWithTheCommand = CcpQueryOptions.INSTANCE
				.startQuery()
				.startBool()
				.startMust()
				.term(JnEntityCommandNotAllowedToUser.Fields.commandName, command.name())
				.endMustAndBackToBool()
				.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		List<CcpJsonRepresentation> ignoredUsers = getIgnoredUsers(requestWithTheCommand);
		return ignoredUsers;
	}

	/**
	 * The ignored users the query finds, each one confirmed by its id and ordered by email.
	 * @param query the query over {@link JnEntityCommandNotAllowedToUser}
	 * @return the ignored users
	 */
	private static List<CcpJsonRepresentation> getIgnoredUsers(CcpQueryOptions query) {

		CcpQueryOptions requestWithAllResults = query.maxResults();
		CcpQueryExecutorDecorator ignoredUsersQuery = requestWithAllResults.selectFrom(JnEntityCommandNotAllowedToUser.ENTITY);
		List<CcpJsonRepresentation> resultList = ignoredUsersQuery.getResultAsList();

		Stream<CcpJsonRepresentation> resultStream = resultList.stream();
		Stream<CcpJsonRepresentation> readableUsersStream = resultStream.map(JnSupportIgnoredUsersList::withReadableEmail);
		Stream<CcpJsonRepresentation> usersWithEmailStream = readableUsersStream.filter(user -> false == user.getAsString(JnJsonCommonsFields.email).isEmpty());
		List<CcpJsonRepresentation> foundUsers = usersWithEmailStream.collect(Collectors.toList());
		boolean nothingFound = foundUsers.isEmpty();

		if(nothingFound) {
			return foundUsers;
		}

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation[] foundUsersArray = foundUsers.toArray(new CcpJsonRepresentation[foundUsers.size()]);
		CcpSelectUnionAll usersById = crud.unionAll(foundUsersArray, JnDeleteKeysFromCache.INSTANCE, JnEntityCommandNotAllowedToUser.ENTITY);
		Stream<CcpJsonRepresentation> usersToConfirmStream = foundUsers.stream();
		Stream<CcpJsonRepresentation> existingUsersStream = usersToConfirmStream.filter(user -> JnEntityCommandNotAllowedToUser.ENTITY.isPresentInThisUnionAll(usersById, user));
		Comparator<CcpJsonRepresentation> byEmail = Comparator.comparing(user -> user.getAsString(JnJsonCommonsFields.email));
		Stream<CcpJsonRepresentation> sortedUsersStream = existingUsersStream.sorted(byEmail);
		List<CcpJsonRepresentation> existingUsers = sortedUsersStream.collect(Collectors.toList());
		return existingUsers;
	}

	/**
	 * Puts in place of the hash of the email the readable one kept in the {@code description} (empty when the user was
	 * ignored before it started being kept there).
	 * @param ignoredUser the record found
	 * @return the record with the readable email
	 */
	private static CcpJsonRepresentation withReadableEmail(CcpJsonRepresentation ignoredUser) {
		CcpJsonRepresentation description = ignoredUser.getInnerJson(JnEntityCommandNotAllowedToUser.Fields.description);
		String readableEmail = description.getAsString(JnJsonCommonsFields.email);
		CcpJsonRepresentation userWithReadableEmail = ignoredUser.put(JnJsonCommonsFields.email, readableEmail);
		return userWithReadableEmail;
	}

	/**
	 * The emails of the ignored users, in the order of the list.
	 * @param ignoredUsers the ignored users
	 * @return the emails
	 */
	public static List<String> getEmails(List<CcpJsonRepresentation> ignoredUsers) {
		Stream<CcpJsonRepresentation> ignoredUsersStream = ignoredUsers.stream();
		Stream<String> emailsStream = ignoredUsersStream.map(user -> user.getAsString(JnJsonCommonsFields.email));
		List<String> emails = emailsStream.collect(Collectors.toList());
		return emails;
	}

	/**
	 * Returns the language of the session (the enum itself or its name), Portuguese by default.
	 * @param json the session
	 * @return the language
	 */
	public static JnLanguage getLanguage(CcpJsonRepresentation json) {
		// the language may be in the json as the enum itself or as its name
		String languageName = json.getAsString(JnJsonCommonsFields.language);
		boolean noLanguage = languageName.isEmpty();
		JnLanguage language = noLanguage ? JnLanguage.portuguese : JnLanguage.valueOf(languageName);
		return language;
	}

	/**
	 * How many users are ignored, followed by the first one.
	 * @param language the language
	 * @param ignoredUsers the ignored users
	 * @return the text
	 */
	static String getListText(JnLanguage language, List<CcpJsonRepresentation> ignoredUsers) {
		int usersCount = ignoredUsers.size();
		boolean singleUser = usersCount == 1;
		JnSupportIgnoredUsersMessages header = singleUser ? JnSupportIgnoredUsersMessages.oneUser : JnSupportIgnoredUsersMessages.manyUsers;
		CcpJsonRepresentation parameters = CcpOtherConstants.EMPTY_JSON.put(JnSupportIgnoredUsersMessageFields.usersCount, usersCount);
		String headerText = header.getMessage(language, parameters);
		String firstUserPrompt = getUserPrompt(language, ignoredUsers, 0);
		String listText = headerText + "\n" + firstUserPrompt;
		return listText;
	}

	/**
	 * Shows the user at the given position (email, the command in which they were ignored and what they had asked)
	 * and asks whether to stop ignoring them or go to the next one.
	 * @param language the language
	 * @param ignoredUsers the ignored users
	 * @param userIndex the position of the user
	 * @return the text
	 */
	static String getUserPrompt(JnLanguage language, List<CcpJsonRepresentation> ignoredUsers, int userIndex) {
		int userNumber = userIndex + 1;
		int usersCount = ignoredUsers.size();
		CcpJsonRepresentation ignoredUser = ignoredUsers.get(userIndex);
		String email = ignoredUser.getAsString(JnJsonCommonsFields.email);
		String commandName = ignoredUser.getAsString(JnEntityCommandNotAllowedToUser.Fields.commandName);
		CcpJsonRepresentation description = ignoredUser.getInnerJson(JnEntityCommandNotAllowedToUser.Fields.description);
		// the summary of the request, written by the cost center of the command (the skill of a skill suggestion, the
		// parent of a skill hierarchy fix, the title of a ticket of the "contact us")
		String request = description.getAsString(JnJsonCommonsFields.request);

		CcpJsonRepresentation parametersWithNumber = CcpOtherConstants.EMPTY_JSON.put(JnSupportIgnoredUsersMessageFields.userNumber, userNumber);
		CcpJsonRepresentation parametersWithCount = parametersWithNumber.put(JnSupportIgnoredUsersMessageFields.usersCount, usersCount);
		CcpJsonRepresentation parametersWithEmail = parametersWithCount.put(JnSupportIgnoredUsersMessageFields.email, email);
		CcpJsonRepresentation parametersWithCommand = parametersWithEmail.put(JnSupportIgnoredUsersMessageFields.commandName, commandName);
		CcpJsonRepresentation parameters = parametersWithCommand.put(JnSupportIgnoredUsersMessageFields.request, request);

		String userPrompt = JnSupportIgnoredUsersMessages.userPrompt.getMessage(language, parameters);
		return userPrompt;
	}

	/**
	 * Returns a message about the user in the language.
	 * @param language the language
	 * @param message the message
	 * @param email the email of the user
	 * @return the message
	 */
	public static String getMessageAboutTheUser(JnLanguage language, JnSupportIgnoredUsersMessages message, String email) {
		CcpJsonRepresentation parameters = CcpOtherConstants.EMPTY_JSON.put(JnSupportIgnoredUsersMessageFields.email, email);
		String messageAboutTheUser = message.getMessage(language, parameters);
		return messageAboutTheUser;
	}

	/**
	 * Ends the session telling the operator that no user is ignored.
	 * @param json the session
	 * @param language the language
	 * @return never returns: throws the flow diversion
	 */
	static CcpJsonRepresentation noIgnoredUser(CcpJsonRepresentation json, JnLanguage language) {
		String noIgnoredUser = JnSupportIgnoredUsersMessages.noIgnoredUser.getMessage(language);
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, noIgnoredUser);
		CcpJsonRepresentation ended = JnSupportIgnoredUsersStatus.noIgnoredUser.throwException(jsonWithReply);
		return ended;
	}
}
