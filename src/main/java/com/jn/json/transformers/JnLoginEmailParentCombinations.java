package com.jn.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityLoginEmail;

/**
 * The parent of a ticket of the user (the "contact us" of js, the skill suggestion and the skill hierarchy fix of vis)
 * among the e-mails of the users ({@link JnEntityLoginEmail}): the e-mail of the record itself, so the group of each user
 * keeps every ticket they opened, whatever its cost center (oneToMany: a ticket has one user).
 */
public class JnLoginEmailParentCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>> {

	/** The single instance. */
	public static final JnLoginEmailParentCombinations INSTANCE = new JnLoginEmailParentCombinations();

	private JnLoginEmailParentCombinations() {}

	/**
	 * The only combination: the e-mail of the record.
	 * @param json the record
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = json.getJsonPiece(JnEntityLoginEmail.Fields.email);
		List<CcpJsonRepresentation> combinations = List.of(combination);
		return combinations;
	}
}
