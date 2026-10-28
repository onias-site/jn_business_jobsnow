package com.jn.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * The scripted update of {@code JnGroupers.removeChildren} on the group of one parent: its own children leave the group,
 * a group left with no child is deleted and a group that does not exist is not created.
 */
class JnGroupersRemoveChildrenItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the children. */
	private final String childName;

	/** The children that leave each group, by parent id. */
	private final Map<String, List<String>> childrenByParentId;

	/**
	 * Keeps what every group loses.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param childrenByParentId the children that leave each group, by parent id
	 */
	JnGroupersRemoveChildrenItem(CcpEntity grouper, String parentName, String childName, Map<String, List<String>> childrenByParentId) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.childrenByParentId = childrenByParentId;
	}

	/**
	 * The scripted update of the group of the parent.
	 * @param parentId the parent id
	 * @return the bulk item
	 */
	public CcpBulkItem apply(String parentId) {
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(this.parentName, this.childName, parentId);
		List<String> children = this.childrenByParentId.get(parentId);
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON.put(JnGroupers.Scripts.childIds, children);
		// the script also runs when the group does not exist, to decide not to create it
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.removeChildren, params, CcpOtherConstants.EMPTY_JSON, true);
		return item;
	}
}
