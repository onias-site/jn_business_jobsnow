package com.jn.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityGrouper;

/**
 * Writes the children of the groups of {@code JnEntityGrouper}, on the side given (the main grouper or its twin). Each
 * group is a single record that keeps only the ids of its children. The writes are atomic scripts run by the database
 * ({@code scriptedUpdate}): two children changing the same group at the same time are applied one after the other, and
 * none overwrites the other. Nothing is read before writing.
 */
public final class JnGroupers {

	/** The scripts run over a group, and their parameters. */
	enum Scripts implements CcpJsonFieldName {
		/** Adds the child id when it is not there yet and recounts; keeps the parent values. */
		addChild {
			public String getValue() {
				return "if (false == ctx._source.child.contains(params.childId)) { ctx._source.child.add(params.childId); } "
						+ "ctx._source.childCount = ctx._source.child.size(); "
						+ "ctx._source.parentJson = params.parentJson;";
			}
		},
		/** Removes the child id; deletes the group left empty; creates nothing when the group does not exist. */
		removeChild {
			public String getValue() {
				return "if (ctx._source.child == null) { ctx.op = 'none'; } "
						+ "else { ctx._source.child.removeIf(c -> c == params.childId); "
						+ "if (ctx._source.child.isEmpty()) { ctx.op = 'delete'; } "
						+ "else { ctx._source.childCount = ctx._source.child.size(); } }";
			}
		},
		/** Parameter: the id of the child being added or removed. */
		childId,
		/** Parameter: the values the parent id came from. */
		parentJson,
	}

	/** How many groups are written at a time. */
	static final int BULK_SIZE = 1_000;

	private JnGroupers() {}

	/**
	 * The key of a group.
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param parentId the parent id
	 * @return the key: parent id, parent name and child name
	 */
	public static CcpJsonRepresentation getGroupKey(String parentName, String childName, String parentId) {
		CcpJsonRepresentation keyWithParentId = CcpOtherConstants.EMPTY_JSON.put(JnEntityGrouper.Fields.parentId, parentId);
		CcpJsonRepresentation keyWithParentName = keyWithParentId.put(JnEntityGrouper.Fields.parentName, parentName);
		CcpJsonRepresentation groupKey = keyWithParentName.put(JnEntityGrouper.Fields.childName, childName);
		return groupKey;
	}

	/**
	 * A side of the grouper.
	 * @param twinSide whether the twin side is wanted
	 * @return the main grouper or its twin
	 */
	public static CcpEntity getGrouper(boolean twinSide) {
		CcpEntity grouper = twinSide ? JnEntityGrouper.ENTITY.getTwinEntity() : JnEntityGrouper.ENTITY;
		return grouper;
	}

	/**
	 * Reads a group on one side of the grouper. The same group can exist on both sides (some children active, others
	 * inactive), and the {@code getOneById} of a twin entity answers {@code REDIRECT} when the record is only on the other
	 * side: this reads both sides by id in a single search and keeps the side asked, so it never redirects.
	 * @param twinSide whether the twin side is the one read
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param parentId the parent id
	 * @return the group, or an empty json when it does not exist on that side
	 */
	public static CcpJsonRepresentation getGroup(boolean twinSide, String parentName, String childName, String parentId) {
		CcpJsonRepresentation groupKey = getGroupKey(parentName, childName, parentId);
		CcpJsonRepresentation groupOnBothSides = JnEntityGrouper.ENTITY.getOneByIdAnyWhere(groupKey);
		CcpEntity grouper = getGrouper(twinSide);
		CcpJsonRepresentation group = groupOnBothSides.getInnerJson(grouper);
		return group;
	}

	/**
	 * The children of a group on one side of the grouper (see {@link #getGroup}).
	 * @param twinSide whether the twin side is the one read
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param parentId the parent id
	 * @return the ids of the children, empty when the group does not exist on that side
	 */
	public static List<String> getChildren(boolean twinSide, String parentName, String childName, String parentId) {
		CcpJsonRepresentation group = getGroup(twinSide, parentName, childName, parentId);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		return children;
	}

	/**
	 * Puts the child in the group of each parent; a group that does not exist yet is created with the child as its only
	 * one, and a child already there is not repeated.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param childId the id of the child
	 * @param parents the values of each parent, by parent id
	 */
	public static void addChild(CcpEntity grouper, String parentName, String childName, String childId, Map<String, CcpJsonRepresentation> parents) {
		CcpJsonRepresentation childIdParams = CcpOtherConstants.EMPTY_JSON.put(Scripts.childId, childId);
		List<String> children = Arrays.asList(childId);
		List<String> parentIds = new ArrayList<>(parents.keySet());

		writeGroups(parentIds, parentId -> {
			CcpJsonRepresentation groupKey = getGroupKey(parentName, childName, parentId);
			CcpJsonRepresentation parentJson = parents.get(parentId);
			CcpJsonRepresentation params = childIdParams.put(Scripts.parentJson, parentJson);
			CcpJsonRepresentation newGroupWithParentJson = groupKey.put(JnEntityGrouper.Fields.parentJson, parentJson);
			CcpJsonRepresentation newGroupWithChildren = newGroupWithParentJson.put(JnEntityGrouper.Fields.child, children);
			CcpJsonRepresentation newGroup = newGroupWithChildren.put(JnEntityGrouper.Fields.childCount, 1);
			CcpBulkItem item = getScriptedItem(grouper, groupKey, Scripts.addChild, params, newGroup, false);
			return item;
		});
	}

	/**
	 * Takes the child out of the group of each parent; a group left with no child is deleted, and a group that does not
	 * exist is not created.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param childId the id of the child
	 * @param parentIds the parent ids
	 */
	public static void removeChild(CcpEntity grouper, String parentName, String childName, String childId, List<String> parentIds) {
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON.put(Scripts.childId, childId);

		writeGroups(parentIds, parentId -> {
			CcpJsonRepresentation groupKey = getGroupKey(parentName, childName, parentId);
			// the script also runs when the group does not exist, to decide not to create it
			CcpBulkItem item = getScriptedItem(grouper, groupKey, Scripts.removeChild, params, CcpOtherConstants.EMPTY_JSON, true);
			return item;
		});
	}

	/**
	 * A scripted update of a group.
	 * @param grouper the side of the grouper (main or twin)
	 * @param groupKey the key of the group
	 * @param script the script
	 * @param params the parameters of the script
	 * @param upsert the group created as is when it does not exist (empty for none)
	 * @param scriptedUpsert whether the script also runs when the group does not exist
	 * @return the bulk item
	 */
	private static CcpBulkItem getScriptedItem(CcpEntity grouper, CcpJsonRepresentation groupKey, Scripts script, CcpJsonRepresentation params, CcpJsonRepresentation upsert, boolean scriptedUpsert) {
		String source = script.getValue();
		String groupId = grouper.calculateId(groupKey);
		CcpJsonRepresentation itemWithScript = CcpOtherConstants.EMPTY_JSON.put(CcpBulkScriptedUpdateFields.script, source);
		CcpJsonRepresentation itemWithParams = itemWithScript.put(CcpBulkScriptedUpdateFields.params, params);
		CcpJsonRepresentation itemWithUpsert = itemWithParams.put(CcpBulkScriptedUpdateFields.upsert, upsert);
		CcpJsonRepresentation item = itemWithUpsert.put(CcpBulkScriptedUpdateFields.scriptedUpsert, scriptedUpsert);
		CcpBulkItem bulkItem = new CcpBulkItem(item, CcpBulkEntityOperationType.scriptedUpdate, grouper, groupId);
		return bulkItem;
	}

	/**
	 * Writes, {@link #BULK_SIZE} at a time, the scripted update of each parent.
	 * @param parentIds the parent ids
	 * @param itemOfTheParent builds the scripted update of the parent
	 */
	private static void writeGroups(List<String> parentIds, Function<String, CcpBulkItem> itemOfTheParent) {
		int parentsCount = parentIds.size();
		for (int start = 0; start < parentsCount; start += BULK_SIZE) {
			int end = Math.min(start + BULK_SIZE, parentsCount);
			List<String> page = parentIds.subList(start, end);
			List<CcpBulkItem> bulkItems = new ArrayList<>();
			for (String parentId : page) {
				CcpBulkItem item = itemOfTheParent.apply(parentId);
				bulkItems.add(item);
			}
			JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems, JnDeleteKeysFromCache.INSTANCE);
		}
	}
}
