package com.jn.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityGrouper;

/**
 * The scripted update of {@code JnGroupers.addChildren} on the group of one parent: each child id enters the group when it
 * is not there yet, and a group that does not exist yet is created with these children.
 */
class JnGroupersAddChildrenItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the children. */
	private final String childName;

	/** The ids of the children of each parent. */
	private final Map<String, List<String>> childrenByParentId;

	/** The values of each parent, by parent id. */
	private final Map<String, CcpJsonRepresentation> parents;

	/**
	 * Keeps what every group receives.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param childrenByParentId the ids of the children of each parent
	 * @param parents the values of each parent, by parent id
	 */
	JnGroupersAddChildrenItem(CcpEntity grouper, String parentName, String childName, Map<String, List<String>> childrenByParentId, Map<String, CcpJsonRepresentation> parents) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.childrenByParentId = childrenByParentId;
		this.parents = parents;
	}

	/**
	 * The scripted update of the group of the parent.
	 * @param parentId the parent id
	 * @return the bulk item
	 */
	public CcpBulkItem apply(String parentId) {
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(this.parentName, this.childName, parentId);
		CcpJsonRepresentation parentJson = this.parents.get(parentId);
		List<String> children = this.childrenByParentId.get(parentId);
		CcpJsonRepresentation childIdsParams = CcpOtherConstants.EMPTY_JSON.put(JnGroupers.Scripts.childIds, children);
		CcpJsonRepresentation params = childIdsParams.put(JnGroupers.Scripts.parentJson, parentJson);
		int childCount = children.size();
		CcpJsonRepresentation newGroupWithParentJson = groupKey.put(JnEntityGrouper.Fields.parentJson, parentJson);
		CcpJsonRepresentation newGroupWithChildren = newGroupWithParentJson.put(JnEntityGrouper.Fields.child, children);
		CcpJsonRepresentation newGroup = newGroupWithChildren.put(JnEntityGrouper.Fields.childCount, childCount);
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.addChildren, params, newGroup, false);
		return item;
	}
}
