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
 * The scripted update of {@code JnGroupers.addChild} on the group of one parent: the child id enters the group when it
 * is not there yet, and a group that does not exist yet is created with the child as its only one.
 */
class JnGroupersAddChildItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the child. */
	private final String childName;

	/** The id of the child. */
	private final String childId;

	/** The parameters of the script without the parent values: the child id. */
	private final CcpJsonRepresentation childIdParams;

	/** The values of each parent, by parent id. */
	private final Map<String, CcpJsonRepresentation> parents;

	/**
	 * Keeps what every group receives.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param childId the id of the child
	 * @param childIdParams the parameters of the script without the parent values
	 * @param parents the values of each parent, by parent id
	 */
	JnGroupersAddChildItem(CcpEntity grouper, String parentName, String childName, String childId, CcpJsonRepresentation childIdParams, Map<String, CcpJsonRepresentation> parents) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.childId = childId;
		this.childIdParams = childIdParams;
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
		CcpJsonRepresentation params = this.childIdParams.put(JnGroupers.Scripts.parentJson, parentJson);
		List<String> children = Arrays.asList(this.childId);
		CcpJsonRepresentation newGroupWithParentJson = groupKey.put(JnEntityGrouper.Fields.parentJson, parentJson);
		CcpJsonRepresentation newGroupWithChildren = newGroupWithParentJson.put(JnEntityGrouper.Fields.child, children);
		CcpJsonRepresentation newGroup = newGroupWithChildren.put(JnEntityGrouper.Fields.childCount, 1);
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.addChild, params, newGroup, false);
		return item;
	}
}
