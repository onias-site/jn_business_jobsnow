package com.jn.utils;

import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * The scripted update of {@code JnGroupers.removeChild} on the group of one parent: the child id leaves the group, a
 * group left with no child is deleted, and a group that does not exist is not created.
 */
class JnGroupersRemoveChildItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the child. */
	private final String childName;

	/** The parameters of the script: the child id. */
	private final CcpJsonRepresentation params;

	/**
	 * Keeps what every group receives.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param params the parameters of the script
	 */
	JnGroupersRemoveChildItem(CcpEntity grouper, String parentName, String childName, CcpJsonRepresentation params) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.params = params;
	}

	/**
	 * The scripted update of the group of the parent.
	 * @param parentId the parent id
	 * @return the bulk item
	 */
	public CcpBulkItem apply(String parentId) {
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(this.parentName, this.childName, parentId);
		// the script also runs when the group does not exist, to decide not to create it
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.removeChild, this.params, CcpOtherConstants.EMPTY_JSON, true);
		return item;
	}
}
