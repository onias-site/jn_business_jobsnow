package com.jn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityGrouper;

/**
 * Writes the children of the groups of {@code JnEntityGrouper}, on the side given (the main grouper or its twin). Each
 * group is a single record that keeps only the ids of its children (or their JSON texts), or, in a group of counters
 * ({@code @CcpEntityGrouperChildCounter}), a single JSON text with one counter per key. {@code childCount} is the size
 * of the {@code child} list, but in a group of counters, where it is the number of keys. The writes are atomic scripts run by the database
 * ({@code scriptedUpdate}): two children changing the same group at the same time are applied one after the other, and
 * none overwrites the other. Nothing is read before writing.
 */
public final class JnGroupers {

	/** The scripts run over a group, and their parameters. */
	enum Scripts implements CcpJsonFieldName {
		/**
		 * The default count of the children, part of every script but {@code incrementCounter}: the size of the
		 * {@code child} list.
		 */
		childCountIsTheSizeOfChild {
			public String getValue() {
				return "ctx._source.childCount = ctx._source.child.size(); ";
			}
		},
		/** Adds the child id when it is not there yet and recounts; keeps the parent values. */
		addChild {
			public String getValue() {
				return "if (false == ctx._source.child.contains(params.childId)) { ctx._source.child.add(params.childId); } "
						+ childCountIsTheSizeOfChild.getValue()
						+ "ctx._source.parentJson = params.parentJson;";
			}
		},
		/**
		 * Adds the child at the end, even when an equal one is already there (a history), drops the oldest ones beyond
		 * {@code maxChildren} and recounts; keeps the parent values.
		 */
		appendChildKeepingTheLast {
			public String getValue() {
				return "ctx._source.child.add(params.childId); "
						+ "while (ctx._source.child.size() > params.maxChildren) { ctx._source.child.remove(0); } "
						+ childCountIsTheSizeOfChild.getValue()
						+ "ctx._source.parentJson = params.parentJson;";
			}
		},
		/** Removes the child id; deletes the group left empty; creates nothing when the group does not exist. */
		removeChild {
			public String getValue() {
				return "if (ctx._source.child == null) { ctx.op = 'none'; } "
						+ "else { ctx._source.child.removeIf(c -> c == params.childId); "
						+ "if (ctx._source.child.isEmpty()) { ctx.op = 'delete'; } "
						+ "else { " + childCountIsTheSizeOfChild.getValue() + "} }";
			}
		},
		/**
		 * Takes out every child that contains all the texts of {@code childKeys} (the earlier version of the same record),
		 * adds the child and recounts; keeps the parent values.
		 */
		replaceChild {
			public String getValue() {
				return "def kept = new ArrayList(); "
						+ "for (def c : ctx._source.child) { boolean sameRecord = true; "
						+ "for (def k : params.childKeys) { if (c.indexOf(k) < 0) { sameRecord = false; } } "
						+ "if (false == sameRecord) { kept.add(c); } } "
						+ "kept.add(params.childId); "
						+ "ctx._source.child = kept; "
						+ childCountIsTheSizeOfChild.getValue()
						+ "ctx._source.parentJson = params.parentJson;";
			}
		},
		/**
		 * Adds 1 to the counter of {@code counterKey} in the only child of the group, a JSON text of counters
		 * ({@code {"key":count,...}}), and keeps the parent values. The exception to {@code childCountIsTheSizeOfChild}:
		 * {@code childCount} is the number of keys, 1 more when the key enters the text. Painless does not parse JSON, so
		 * the text is changed as text: the counter of the key is the digits after {@code "key":} (a key holds no quote).
		 */
		incrementCounter {
			public String getValue() {
				return "String key = '\"' + params.counterKey + '\":'; "
						+ "String counters = ctx._source.child[0]; "
						+ "int keyStart = counters.indexOf(key); "
						+ "if (keyStart < 0) { "
						+ "String separator = counters.length() > 2 ? ',' : ''; "
						+ "ctx._source.child[0] = counters.substring(0, counters.length() - 1) + separator + key + '1}'; "
						+ "ctx._source.childCount = ctx._source.childCount + 1; } "
						+ "else { "
						+ "int countStart = keyStart + key.length(); "
						+ "int countEnd = countStart; "
						+ "while (countEnd < counters.length() && Character.isDigit(counters.charAt(countEnd))) { countEnd++; } "
						+ "int count = Integer.parseInt(counters.substring(countStart, countEnd)); "
						+ "ctx._source.child[0] = counters.substring(0, countStart) + (count + 1) + counters.substring(countEnd); } "
						+ "ctx._source.parentJson = params.parentJson;";
			}
		},
		/** Parameter: the id of the child being added or removed. */
		childId,
		/** Parameter: the key of the counter being incremented. */
		counterKey,
		/** Parameter: the most children a history keeps. */
		maxChildren,
		/** Parameter: the texts that only the children of the same record contain. */
		childKeys,
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
		CcpEntityPhase side = twinSide ? CcpEntityPhase.twinEntity : CcpEntityPhase.mainEntity;
		CcpEntity grouper = side.getEntity(JnEntityGrouper.ENTITY);
		return grouper;
	}

	/**
	 * Reads a group on one side of the grouper, through the common read of the groups ({@code JnBusinessGetGroup}).
	 * @param twinSide whether the twin side is the one read
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param parentId the parent id
	 * @return the group, or an empty json when it does not exist on that side
	 */
	public static CcpJsonRepresentation getGroup(boolean twinSide, String parentName, String childName, String parentId) {
		CcpJsonRepresentation groupKey = getGroupKey(parentName, childName, parentId);
		CcpEntityPhase side = twinSide ? CcpEntityPhase.twinEntity : CcpEntityPhase.mainEntity;
		CcpJsonRepresentation groupKeyAndSide = groupKey.put(JnBusinessGetGroup.Fields.side, side.name());
		CcpJsonRepresentation group = JnBusinessGetGroup.INSTANCE.execute(groupKeyAndSide);
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
		Set<String> parentIdsSet = parents.keySet();
		List<String> parentIds = new ArrayList<>(parentIdsSet);
		JnGroupersAddChildItem itemOfTheParent = new JnGroupersAddChildItem(grouper, parentName, childName, childId, childIdParams, parents);
		writeGroups(parentIds, itemOfTheParent);
	}

	/**
	 * The bulk items that add the child at the end of the group of each parent, to be written with the other items of the
	 * caller: a history, so an equal child already there is kept and the new one enters too, and only the last
	 * {@code maxChildren} stay; a group that does not exist yet is created with the child as its only one.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param child the child
	 * @param maxChildren the most children the group keeps
	 * @param parents the values of each parent, by parent id
	 * @return one bulk item per parent
	 */
	public static List<CcpBulkItem> getItemsToAppendChildKeepingTheLast(CcpEntity grouper, String parentName, String childName, String child, int maxChildren, Map<String, CcpJsonRepresentation> parents) {
		CcpJsonRepresentation childParams = CcpOtherConstants.EMPTY_JSON.put(Scripts.childId, child);
		CcpJsonRepresentation childAndMaxParams = childParams.put(Scripts.maxChildren, maxChildren);
		JnGroupersAppendChildItem itemOfTheParent = new JnGroupersAppendChildItem(grouper, parentName, childName, child, childAndMaxParams, parents);
		List<CcpBulkItem> items = new ArrayList<>();
		Set<String> parentIds = parents.keySet();
		for (String parentId : parentIds) {
			CcpBulkItem item = itemOfTheParent.apply(parentId);
			items.add(item);
		}
		return items;
	}

	/**
	 * Puts the child in the group of each parent in place of the earlier version of the same record (the children that
	 * contain every text of {@code childKeys}); a group that does not exist yet is created with the child as its only one.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the child
	 * @param childKeys the texts that only the children of the same record contain
	 * @param child the child
	 * @param parents the values of each parent, by parent id
	 */
	public static void replaceChild(CcpEntity grouper, String parentName, String childName, List<String> childKeys, String child, Map<String, CcpJsonRepresentation> parents) {
		CcpJsonRepresentation childParams = CcpOtherConstants.EMPTY_JSON.put(Scripts.childId, child);
		CcpJsonRepresentation childAndKeysParams = childParams.put(Scripts.childKeys, childKeys);
		Set<String> parentIdsSet = parents.keySet();
		List<String> parentIds = new ArrayList<>(parentIdsSet);
		JnGroupersReplaceChildItem itemOfTheParent = new JnGroupersReplaceChildItem(grouper, parentName, childName, child, childAndKeysParams, parents);
		writeGroups(parentIds, itemOfTheParent);
	}

	/**
	 * The counters of a group of counters ({@code @CcpEntityGrouperChildCounter}), read by id on the main side of the
	 * grouper: its only child, a JSON text, parsed.
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the counted records
	 * @param parentId the parent id
	 * @return the counter of each key, empty when the group does not exist
	 */
	public static CcpJsonRepresentation getCounters(String parentName, String childName, String parentId) {
		CcpJsonRepresentation group = getGroup(false, parentName, childName, parentId);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		boolean noCounter = children.isEmpty();

		if(noCounter) {
			return CcpOtherConstants.EMPTY_JSON;
		}

		String countersText = children.get(0);
		CcpJsonRepresentation counters = new CcpJsonRepresentation(countersText);
		return counters;
	}

	/**
	 * Adds 1 to the counter of the key in the group of counters of each parent; a group that does not exist yet is created
	 * with this counter at 1.
	 * @param grouper the side of the grouper (main or twin)
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the counted record
	 * @param counterKey the key of the counter
	 * @param parents the values of each parent, by parent id
	 */
	public static void incrementCounter(CcpEntity grouper, String parentName, String childName, String counterKey, Map<String, CcpJsonRepresentation> parents) {
		CcpJsonRepresentation counterKeyParams = CcpOtherConstants.EMPTY_JSON.put(Scripts.counterKey, counterKey);
		Set<String> parentIdsSet = parents.keySet();
		List<String> parentIds = new ArrayList<>(parentIdsSet);
		JnGroupersIncrementCounterItem itemOfTheParent = new JnGroupersIncrementCounterItem(grouper, parentName, childName, counterKeyParams, parents);
		writeGroups(parentIds, itemOfTheParent);
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
		JnGroupersRemoveChildItem itemOfTheParent = new JnGroupersRemoveChildItem(grouper, parentName, childName, params);
		writeGroups(parentIds, itemOfTheParent);
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
	static CcpBulkItem getScriptedItem(CcpEntity grouper, CcpJsonRepresentation groupKey, Scripts script, CcpJsonRepresentation params, CcpJsonRepresentation upsert, boolean scriptedUpsert) {
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
			JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems, JnDoNotDeleteKeysFromCache.INSTANCE);
		}
	}
}
