package com.jn.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityGrouper;

/**
 * The scripted update of {@code JnGroupers.incrementCounter} on the group of one parent: the counter of the key gets 1
 * more, and a group that does not exist yet is created with an empty JSON text, on which the script then runs.
 */
class JnGroupersIncrementCounterItem implements Function<String, CcpBulkItem> {

	/** The side of the grouper (main or twin). */
	private final CcpEntity grouper;

	/** The kind of group. */
	private final String parentName;

	/** The name of the entity of the counted record. */
	private final String childName;

	/** The parameters of the script without the parent values: the key of the counter. */
	private final CcpJsonRepresentation counterKeyParams;

	/** The values of each parent, by parent id. */
	private final Map<String, CcpJsonRepresentation> parents;

	/**
	 * Keeps what every group receives.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the counted record
	 * @param counterKeyParams the parameters of the script without the parent values
	 * @param parents the values of each parent, by parent id
	 */
	JnGroupersIncrementCounterItem(CcpEntity grouper, String parentName, String childName, CcpJsonRepresentation counterKeyParams, Map<String, CcpJsonRepresentation> parents) {
		this.grouper = grouper;
		this.parentName = parentName;
		this.childName = childName;
		this.counterKeyParams = counterKeyParams;
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
		CcpJsonRepresentation params = this.counterKeyParams.put(JnGroupers.Scripts.parentJson, parentJson);
		// the group is created with no counter and the script also runs on it, so every counter is written by the script
		String noCounter = CcpOtherConstants.EMPTY_JSON.asUgglyJson();
		List<String> children = Arrays.asList(noCounter);
		CcpJsonRepresentation newGroupWithParentJson = groupKey.put(JnEntityGrouper.Fields.parentJson, parentJson);
		CcpJsonRepresentation newGroupWithChildren = newGroupWithParentJson.put(JnEntityGrouper.Fields.child, children);
		CcpJsonRepresentation newGroup = newGroupWithChildren.put(JnEntityGrouper.Fields.childCount, 0);
		CcpBulkItem item = JnGroupers.getScriptedItem(this.grouper, groupKey, JnGroupers.Scripts.incrementCounter, params, newGroup, true);
		return item;
	}
}
