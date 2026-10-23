package com.jn.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityGrouper;

/**
 * The scripted update of {@code JnGroupers.getItemsToAppendChildKeepingTheLast} on the group of one parent: the child
 * enters at the end of the group, only the last ones up to the maximum stay, and a group that does not exist yet is
 * created with the child as its only one.
 */
class JnGroupersAppendChildItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the child. */
	private final String childName;

	/** The child. */
	private final String child;

	/** The parameters of the script without the parent values: the child and the maximum of children. */
	private final CcpJsonRepresentation childParams;

	/** The values of each parent, by parent id. */
	private final Map<String, CcpJsonRepresentation> parents;

	/**
	 * Keeps what every group receives.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param child the child
	 * @param childParams the parameters of the script without the parent values
	 * @param parents the values of each parent, by parent id
	 */
	JnGroupersAppendChildItem(CcpEntity grouper, String parentName, String childName, String child, CcpJsonRepresentation childParams, Map<String, CcpJsonRepresentation> parents) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.child = child;
		this.childParams = childParams;
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
		CcpJsonRepresentation params = this.childParams.put(JnGroupers.Scripts.parentJson, parentJson);
		List<String> children = Arrays.asList(this.child);
		CcpJsonRepresentation newGroupWithParentJson = groupKey.put(JnEntityGrouper.Fields.parentJson, parentJson);
		CcpJsonRepresentation newGroupWithChildren = newGroupWithParentJson.put(JnEntityGrouper.Fields.child, children);
		CcpJsonRepresentation newGroup = newGroupWithChildren.put(JnEntityGrouper.Fields.childCount, 1);
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.appendChildKeepingTheLast, params, newGroup, false);
		return item;
	}
}
