package com.jn.business.bots.ignored.users;

import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.utils.JnLanguage;

/**
 * First step of the {@code ignoredUsers} command ({@code /ignoredUsers}): tells the operator how many users the
 * support is ignoring and shows the first one, asking whether to stop ignoring them or go to the next one, the same
 * interaction of {@code /pendingTickets}. Without any ignored user the session ends with that notice
 * ({@code noIgnoredUser}).
 */
public class JnSupportIgnoredUsersShow implements CcpBusiness {
	/** The single instance: the class keeps no state. */
	public final static JnSupportIgnoredUsersShow INSTANCE = new JnSupportIgnoredUsersShow();

	/** Singleton; use {@link #INSTANCE}, or extend it to show another list. */
	protected JnSupportIgnoredUsersShow() {}

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
	 * Shows the count and the first ignored user.
	 * @param json the session
	 * @return the session with the reply and the user shown
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		List<CcpJsonRepresentation> ignoredUsers = this.getIgnoredUsers();
		JnLanguage language = JnSupportIgnoredUsersList.getLanguage(json);
		boolean noIgnoredUser = ignoredUsers.isEmpty();

		if(noIgnoredUser) {
			CcpJsonRepresentation ended = JnSupportIgnoredUsersList.noIgnoredUser(json, language);
			return ended;
		}

		String listText = JnSupportIgnoredUsersList.getListText(language, ignoredUsers);
		List<String> emails = JnSupportIgnoredUsersList.getEmails(ignoredUsers);
		String firstEmail = emails.get(0);
		CcpJsonRepresentation jsonWithReply = json.put(JnJsonInstantMessengerFields.botReply, listText);
		CcpJsonRepresentation jsonWithTheUser = jsonWithReply.put(JnSupportIgnoredUsersFields.currentUser, firstEmail);
		return jsonWithTheUser;
	}
}
