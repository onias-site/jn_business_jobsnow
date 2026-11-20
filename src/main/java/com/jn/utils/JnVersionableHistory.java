package com.jn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityVirtualVersionableHistory;

/**
 * The history of the records of the versionable entities ({@code @CcpEntityVersionable}), kept in {@code jn_grouper}: one
 * group per record, whose parent is {@link JnEntityVirtualVersionableHistory} (the name of the entity and the document id
 * of the record), whose {@code childName} is the name of the entity of the record and whose children are the versions of
 * the record, one JSON text per write ({@code operation}, {@code date}, {@code timestamp} and {@code json}, the record
 * before the write), in the order of the writes. The groups are always on the main side of the grouper, whatever the side
 * of the record: the twin of a versionable entity has its own name, so its records have groups of their own.
 * <p>
 * Until 2026-11-20 each version was a row of {@code jn_versionable}, found only by a query.
 */
public final class JnVersionableHistory {

	/** The fields this class adds to the groups and to the versions it hands over. */
	public static enum Fields implements CcpJsonFieldName {
		/** In the {@code parentJson} of the group: the values of the primary key of the record. */
		primaryKey,
		/** In the history read by id: the versions of the record, oldest first. */
		versions,
	}

	/** The constants of the readings. */
	static enum Constants implements CcpJsonFieldName {
		/** How long each page of a scroll over the groups stays open. */
		SCROLL_TIME { public String getValue() { return "1m"; } },
	}

	/** The most versions a record keeps: beyond them, the oldest ones leave the group. */
	public static final int MAX_VERSIONS = 1_000;

	/** How many groups each page of a scroll brings. */
	static final int PAGE_SIZE = 500;

	private JnVersionableHistory() {}

	/**
	 * The kind of the groups of the history.
	 * @return the name of {@link JnEntityVirtualVersionableHistory}
	 */
	public static String getParentName() {
		CcpEntityMetaData parentDetails = JnEntityVirtualVersionableHistory.ENTITY.getEntityMetaData();
		return parentDetails.entityName;
	}

	/**
	 * The values the parent id of the history of a record comes from.
	 * @param entityName the name of the entity of the record
	 * @param recordId the document id of the record
	 * @return {@code entity} and {@code id}
	 */
	private static CcpJsonRepresentation getParentKey(String entityName, String recordId) {
		CcpJsonRepresentation parentKeyWithEntity = CcpOtherConstants.EMPTY_JSON.put(JnEntityVirtualVersionableHistory.Fields.entity, entityName);
		CcpJsonRepresentation parentKey = parentKeyWithEntity.put(JnEntityVirtualVersionableHistory.Fields.id, recordId);
		return parentKey;
	}

	/**
	 * The parent id of the history of a record.
	 * @param entityName the name of the entity of the record
	 * @param recordId the document id of the record
	 * @return the parent id
	 */
	public static String getParentId(String entityName, String recordId) {
		CcpJsonRepresentation parentKey = getParentKey(entityName, recordId);
		String parentId = JnEntityVirtualVersionableHistory.ENTITY.calculateId(parentKey);
		return parentId;
	}

	/**
	 * The key of the group of the history of a record.
	 * @param entityName the name of the entity of the record
	 * @param recordId the document id of the record
	 * @return the key of the group
	 */
	public static CcpJsonRepresentation getGroupKey(String entityName, String recordId) {
		String parentName = getParentName();
		String parentId = getParentId(entityName, recordId);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentName, entityName, parentId);
		return groupKey;
	}

	/**
	 * The bulk item that adds a version at the end of the history of a record (creating the group with it when the record
	 * has no history yet), to be written with the other items of the write.
	 * @param entityName the name of the entity of the record
	 * @param recordId the document id of the record
	 * @param primaryKey the values of the primary key of the record
	 * @param version the version: {@code operation}, {@code date}, {@code timestamp} and {@code json}
	 * @return the bulk items (one)
	 */
	public static List<CcpBulkItem> getItemsToAppendTheVersion(String entityName, String recordId, CcpJsonRepresentation primaryKey, CcpJsonRepresentation version) {
		CcpJsonRepresentation parentKey = getParentKey(entityName, recordId);
		CcpJsonRepresentation parentJson = parentKey.put(Fields.primaryKey, primaryKey);
		String parentId = JnEntityVirtualVersionableHistory.ENTITY.calculateId(parentKey);
		Map<String, CcpJsonRepresentation> parents = Map.of(parentId, parentJson);
		String child = version.asUgglyJson();
		CcpEntity grouper = JnGroupers.getGrouper(false);
		String parentName = getParentName();
		List<CcpBulkItem> items = JnGroupers.getItemsToAppendChildKeepingTheLast(grouper, parentName, entityName, child, MAX_VERSIONS, parents);
		return items;
	}

	/**
	 * The bulk item that deletes the history of a record.
	 * @param entityName the name of the entity of the record
	 * @param recordId the document id of the record
	 * @return the bulk item
	 */
	public static CcpBulkItem getItemToDeleteTheHistory(String entityName, String recordId) {
		CcpJsonRepresentation groupKey = getGroupKey(entityName, recordId);
		CcpEntity grouper = JnGroupers.getGrouper(false);
		String groupId = grouper.calculateId(groupKey);
		CcpBulkItem item = new CcpBulkItem(groupKey, CcpBulkEntityOperationType.delete, grouper, groupId);
		return item;
	}

	/**
	 * The versions of a group of the history, oldest first.
	 * @param group the group
	 * @return each child parsed
	 */
	public static List<CcpJsonRepresentation> getVersions(CcpJsonRepresentation group) {
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		List<CcpJsonRepresentation> versions = new ArrayList<>();
		for (String child : children) {
			CcpJsonRepresentation version = new CcpJsonRepresentation(child);
			versions.add(version);
		}
		return versions;
	}

	/**
	 * Hands over every version of the records of an entity written since the given time, scrolling the groups of the
	 * history of that entity (a query: for scheduled processes, never for online requests). Each version comes with the
	 * {@code parentJson} of its group: {@code entity}, {@code id} and {@code primaryKey}.
	 * @param entityName the name of the entity of the records
	 * @param since the oldest {@code timestamp} handed over (0 for every version)
	 * @param versionConsumer receives each version
	 */
	public static void consumeVersions(String entityName, long since, Consumer<CcpJsonRepresentation> versionConsumer) {
		String parentName = getParentName();
		CcpQueryOptions query = CcpQueryOptions.INSTANCE
				.startQuery()
					.startBool()
						.startMust()
							.term(JnEntityGrouper.Fields.parentName, parentName)
							.term(JnEntityGrouper.Fields.childName, entityName)
						.endMustAndBackToBool()
					.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		Consumer<CcpJsonRepresentation> groupReader = new JnVersionableHistoryGroupReader(since, versionConsumer);
		String scrollTime = Constants.SCROLL_TIME.getValue();
		String parentJsonField = JnEntityGrouper.Fields.parentJson.getValue();
		String childField = JnEntityGrouper.Fields.child.getValue();
		query.selectFrom(JnEntityGrouper.ENTITY).consumeQueryResult(scrollTime, PAGE_SIZE, groupReader, parentJsonField, childField);
	}
}
