package com.jn.business.bots.ignored.users;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.business.bots.JnSupportBotCommands;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;

/**
 * Second step of the {@code ignoredUsers} command, repeated until the operator chooses a user: {@code 1} stops
 * ignoring the user shown, {@code 2} shows the next one (after the last one, the first one again). Any other answer
 * shows the same user again ({@code invalidAnswer}).
 *
 * <p>Choosing the user ends this session ({@code userChosen}) and asks the bot to handle
 * {@code /allowCommandToUser <email>} as if the operator had typed it ({@link JnJsonInstantMessengerFields#typedValueToRedirect}),
 * with this command as the list to show again ({@link JnJsonInstantMessengerFields#listToShowAgain}), so that the remaining
 * ignored users are listed when it finishes. The list is read again at every answer: a user shown that was allowed in
 * the meantime is not allowed twice; the operator is told so and sees the first one.
 */
public class JnSupportIgnoredUsersChoose implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JnSupportIgnoredUsersChoose INSTANCE = new JnSupportIgnoredUsersChoose();

	/** Singleton; use {@link #INSTANCE}, or extend it to choose from another list. */
	protected JnSupportIgnoredUsersChoose() {}

	/**
	 * The ignored users this list shows: every one of them. A command that lists only the users ignored in one command
	 * ({@code /ignoredContactUs} of js) overrides it.
	 * @return the ignored users
	 */
	protected List<CcpJsonRepresentation> getIgnoredUsers() {
		List<CcpJsonRepresentation> ignoredUsers = JnSupportIgnoredUsersList.getIgnoredUsers();
		return ignoredUsers;
	}

	/**
	 * The command shown again when the {@code /allowCommandToUser} chosen here finishes: this one.
	 * @return the command, with its slash
	 */
	protected String getThisCommand() {
		String thisCommand = "/" + JnSupportBotCommands.ignoredUsers;
		return thisCommand;
	}

	/** The answer that stops ignoring the user shown. */
	private static final String ALLOW = "1";

	/** The answer that shows the next user. */
	private static final String NEXT = "2";

	/**
	 * Handles the answer of the operator: stops ignoring the user shown (redirecting to {@code /allowCommandToUser}),
	 * shows the next one (going back to the first after the last), or asks again.
	 * @param json the session, with the user shown and the answer
	 * @return the session with the reply of the bot
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		List<CcpJsonRepresentation> ignoredUsers = this.getIgnoredUsers();
		JnLanguage language = JnSupportIgnoredUsersList.getLanguage(json);
		boolean noIgnoredUser = ignoredUsers.isEmpty();

		if(noIgnoredUser) {
			CcpJsonRepresentation ended = JnSupportIgnoredUsersList.noIgnoredUser(json, language);
			return ended;
		}

		List<String> emails = JnSupportIgnoredUsersList.getEmails(ignoredUsers);
		String currentUser = json.getAsString(JnSupportIgnoredUsersFields.currentUser);
		int currentIndex = emails.indexOf(currentUser);
		boolean allowedInTheMeantime = currentIndex < 0;
		String typedValue = json.getAsString(JnJsonCommonsFields.typedValue);
		String answer = typedValue.trim();

		if(allowedInTheMeantime) {
			String alreadyAllowed = JnSupportIgnoredUsersList.getMessageAboutTheUser(language, JnSupportIgnoredUsersMessages.userAlreadyAllowed, currentUser);
			CcpJsonRepresentation showingTheFirst = this.showUser(json, language, ignoredUsers, 0, alreadyAllowed);
			return showingTheFirst;
		}

		boolean allow = ALLOW.equals(answer);

		if(allow) {
			String userChosen = JnSupportIgnoredUsersList.getMessageAboutTheUser(language, JnSupportIgnoredUsersMessages.userChosen, currentUser);
			String allowCommand = "/" + JnSupportBotCommands.allowCommandToUser + " " + currentUser;
			String thisCommand = this.getThisCommand();
			CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, userChosen);
			CcpJsonRepresentation jsonWithTheCommand = jsonWithReply.put(JnJsonInstantMessengerFields.typedValueToRedirect, allowCommand);
			CcpJsonRepresentation jsonWithTheListToShowAgain = jsonWithTheCommand.put(JnJsonInstantMessengerFields.listToShowAgain, thisCommand);
			CcpJsonRepresentation chosen = JnSupportIgnoredUsersStatus.userChosen.throwException(jsonWithTheListToShowAgain);
			return chosen;
		}

		boolean next = NEXT.equals(answer);

		if(next) {
			int nextIndex = (currentIndex + 1) % ignoredUsers.size();
			CcpJsonRepresentation showingTheNext = this.showUser(json, language, ignoredUsers, nextIndex, "");
			return showingTheNext;
		}

		String notUnderstood = JnSupportIgnoredUsersMessages.notUnderstood.getMessage(language);
		CcpJsonRepresentation showingTheSame = this.showUser(json, language, ignoredUsers, currentIndex, notUnderstood);
		CcpJsonRepresentation askedAgain = JnSupportIgnoredUsersStatus.invalidAnswer.throwException(showingTheSame);
		return askedAgain;
	}

	/**
	 * Puts in the session the reply that shows the user at the position, after a notice.
	 * @param json the session
	 * @param language the language of the reply
	 * @param ignoredUsers the ignored users
	 * @param userIndex the position of the user
	 * @param notice the notice before the user, possibly empty
	 * @return the session with the reply and the user shown
	 */
	private CcpJsonRepresentation showUser(CcpJsonRepresentation json, JnLanguage language, List<CcpJsonRepresentation> ignoredUsers, int userIndex, String notice) {
		String userPrompt = JnSupportIgnoredUsersList.getUserPrompt(language, ignoredUsers, userIndex);
		String reply = notice + userPrompt;
		CcpJsonRepresentation ignoredUser = ignoredUsers.get(userIndex);
		String email = ignoredUser.getAsString(JnJsonCommonsFields.email);
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, reply);
		CcpJsonRepresentation jsonWithTheUser = jsonWithReply.put(JnSupportIgnoredUsersFields.currentUser, email);
		return jsonWithTheUser;
	}
}
