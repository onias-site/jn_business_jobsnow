package com.jn.business.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityVirtualDisposableRecordToPurge;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * The daily purge of the expired records of the disposable entities whose {@code @CcpEntityDisposable} has
 * {@code removeCompletely} (the default). It is a scheduled process, so it may run queries (OQL).
 * <p>
 * Every write of such a record keeps the document of its period in the group of the record
 * ({@link JnEntityVirtualDisposableRecordToPurge}, in {@code JnEntityGrouper}), as a JSON text with the index
 * ({@code entity}), the {@code id} of the document and the end of its period ({@code timestamp}). The purge reads every
 * one of these groups and, for the documents whose period has ended:
 * <ol>
 * <li>deletes them from their indexes;</li>
 * <li>deletes the copy of the record in {@link JnEntityDisposableRecord} when every document of the group has expired and
 * the copy has expired too: a copy expires one period after its write, so it may still be valid after the end of the
 * period of its document; then the group keeps its expired children until a later purge finds the copy expired;</li>
 * <li>takes them out of the group, by an atomic script that deletes the group left empty (a document written after the
 * read stays in it).</li>
 * </ol>
 * The entities with {@code removeCompletely = false} never enter the groups, so nothing of theirs is purged.
 * <p>
 * Output: {@code groupsRead}, {@code expiredDocuments}, {@code purgedDocuments} and {@code purgedCopies}.
 */
public class JnBusinessPurgeExpiredDisposableRecords implements CcpBusiness {

	/** The fields of the output. */
	public static enum Fields implements CcpJsonFieldName {
		/** How many groups of records to purge were read. */
		groupsRead,
		/** How many documents of the groups had expired. */
		expiredDocuments,
		/** How many expired documents were deleted from their indexes. */
		purgedDocuments,
		/** How many expired copies were deleted from {@code jn_disposable_record}. */
		purgedCopies,
		/** The {@code deleted} counter of a delete by query. */
		deleted,
		/** The HTTP status of a failed request to the database. */
		status,
	}

	/** The HTTP status of a delete by query on an index that does not exist. */
	static final int NOT_FOUND = 404;

	/** The texts of this class. */
	enum Constants implements CcpJsonFieldName {
		/** How long the scroll of the groups is kept between two pages. */
		SCROLL_TIME { public String getValue() { return "1m"; } },
	}

	/** How many groups each page of the scroll brings, and how many ids each delete by query takes. */
	static final int PAGE_SIZE = 1_000;

	/** The single instance: the business keeps no state. */
	public static final JnBusinessPurgeExpiredDisposableRecords INSTANCE = new JnBusinessPurgeExpiredDisposableRecords();

	private JnBusinessPurgeExpiredDisposableRecords() {}

	/**
	 * Purges the expired documents of every group of records to purge.
	 * @param json not read
	 * @return the counters of the purge
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		long now = System.currentTimeMillis();
		CcpEntityMetaData parentDetails = JnEntityVirtualDisposableRecordToPurge.ENTITY.getEntityMetaData();
		CcpEntityMetaData copyDetails = JnEntityDisposableRecord.ENTITY.getEntityMetaData();
		List<CcpJsonRepresentation> groups = getGroupsToPurge(parentDetails.entityName, copyDetails.entityName);

		Map<String, List<String>> expiredIdsByEntity = new LinkedHashMap<>();
		Map<String, List<String>> expiredChildrenByParentId = new LinkedHashMap<>();
		Map<String, String> parentIdByCopyIdToPurge = new LinkedHashMap<>();
		int expiredDocuments = 0;
		for (CcpJsonRepresentation group : groups) {
			List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
			List<String> expiredChildren = new ArrayList<>();
			for (String child : children) {
				CcpJsonRepresentation document = new CcpJsonRepresentation(child);
				Long endOfThePeriod = document.getAsLongNumber(JnJsonCommonsFields.timestamp);
				boolean stillValid = endOfThePeriod > now;

				if(stillValid) {
					continue;
				}

				String entityName = document.getAsString(JnJsonCommonsFields.entity);
				String documentId = document.getAsString(JnJsonCommonsFields.id);
				List<String> expiredIds = expiredIdsByEntity.computeIfAbsent(entityName, newEntityName -> new ArrayList<>());
				expiredIds.add(documentId);
				expiredChildren.add(child);
			}
			boolean nothingExpired = expiredChildren.isEmpty();

			if(nothingExpired) {
				continue;
			}

			expiredDocuments += expiredChildren.size();
			String parentId = group.getAsString(JnEntityGrouper.Fields.parentId);
			expiredChildrenByParentId.put(parentId, expiredChildren);
			boolean someChildStillValid = expiredChildren.size() < children.size();

			if(someChildStillValid) {
				continue;
			}

			CcpJsonRepresentation copyKey = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
			String copyId = JnEntityDisposableRecord.ENTITY.calculateId(copyKey);
			parentIdByCopyIdToPurge.put(copyId, parentId);
		}

		long purgedDocuments = 0;
		Set<String> entityNames = expiredIdsByEntity.keySet();
		for (String entityName : entityNames) {
			List<String> expiredIds = expiredIdsByEntity.get(entityName);
			purgedDocuments += deleteByIds(entityName, expiredIds, Long.MAX_VALUE);
		}
		// a copy expires one period after its write, later than the end of the period of its document: while it is valid,
		// its group keeps the expired children, so that the next purge finds the copy again
		Set<String> copyIds = parentIdByCopyIdToPurge.keySet();
		List<String> copyIdsToPurge = new ArrayList<>(copyIds);
		Set<String> idsOfValidCopies = getIdsOfValidCopies(copyDetails.entityName, copyIdsToPurge, now);
		for (String idOfValidCopy : idsOfValidCopies) {
			String parentIdOfValidCopy = parentIdByCopyIdToPurge.get(idOfValidCopy);
			expiredChildrenByParentId.remove(parentIdOfValidCopy);
		}
		// the copy is deleted only when it has expired as well: a write after the read of the groups renews it
		long purgedCopies = deleteByIds(copyDetails.entityName, copyIdsToPurge, now);
		CcpEntity grouper = JnGroupers.getGrouper(false);
		JnGroupers.removeChildren(grouper, parentDetails.entityName, copyDetails.entityName, expiredChildrenByParentId);

		int groupsRead = groups.size();
		CcpJsonRepresentation resultWithGroups = json.put(Fields.groupsRead, groupsRead);
		CcpJsonRepresentation resultWithExpired = resultWithGroups.put(Fields.expiredDocuments, expiredDocuments);
		CcpJsonRepresentation resultWithDocuments = resultWithExpired.put(Fields.purgedDocuments, purgedDocuments);
		CcpJsonRepresentation result = resultWithDocuments.put(Fields.purgedCopies, purgedCopies);
		return result;
	}

	/**
	 * Every group of records to purge, on the main side of the grouper: its parent id, the key of the copy it came from and
	 * its children.
	 * @param parentName the name of the parent entity of the groups
	 * @param childName the name of the entity of the copies
	 * @return the groups
	 */
	private static List<CcpJsonRepresentation> getGroupsToPurge(String parentName, String childName) {
		CcpQueryOptions groupsToPurge = CcpQueryOptions.INSTANCE
				.startQuery()
					.startBool()
						.startMust()
							.term(JnEntityGrouper.Fields.parentName, parentName)
							.term(JnEntityGrouper.Fields.childName, childName)
						.endMustAndBackToBool()
					.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		List<CcpJsonRepresentation> groups = new ArrayList<>();
		String scrollTime = Constants.SCROLL_TIME.getValue();
		String parentIdField = JnEntityGrouper.Fields.parentId.getValue();
		String parentJsonField = JnEntityGrouper.Fields.parentJson.getValue();
		String childField = JnEntityGrouper.Fields.child.getValue();
		CcpQueryExecutorDecorator groupsOfTheGrouper = groupsToPurge.selectFrom(JnEntityGrouper.ENTITY);
		groupsOfTheGrouper.consumeQueryResult(scrollTime, PAGE_SIZE, groups::add, parentIdField, parentJsonField, childField);
		return groups;
	}

	/**
	 * The ids of the copies, among the given ones, still valid ({@code timestamp} after now). A hit of a search carries
	 * the id of its document under {@code id}, in place of the field of the copy with the same name.
	 * @param copyName the index of the copies
	 * @param copyIds the ids of the copies
	 * @param now the instant of the purge
	 * @return the ids of the valid copies
	 */
	private static Set<String> getIdsOfValidCopies(String copyName, List<String> copyIds, long now) {
		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		String idFieldName = dbRequester.getFieldNameToId();
		CcpFieldName idField = new CcpFieldName(idFieldName);
		String timestampField = JnJsonCommonsFields.timestamp.getValue();
		String scrollTime = Constants.SCROLL_TIME.getValue();
		String documentIdField = JnJsonCommonsFields.id.getValue();
		Set<String> validCopyIds = new HashSet<>();
		int idsCount = copyIds.size();
		for (int start = 0; start < idsCount; start += PAGE_SIZE) {
			int end = Math.min(start + PAGE_SIZE, idsCount);
			List<String> page = new ArrayList<>(copyIds.subList(start, end));
			CcpQueryOptions validCopiesOfThePage = CcpQueryOptions.INSTANCE
					.startQuery()
						.startBool()
							.startMust()
								.terms(idField, page)
								.startRange()
									.startFieldRange(timestampField)
										.greaterThan(now)
									.endFieldRangeAndBackToRange()
								.endRangeAndBackToMust()
							.endMustAndBackToBool()
						.endBoolAndBackToQuery()
					.endQueryAndBackToRequest();
			CcpQueryExecutorDecorator validCopies = validCopiesOfThePage.selectFrom(copyName);
			validCopies.consumeQueryResult(scrollTime, PAGE_SIZE, copy -> validCopyIds.add(copy.getAsString(JnJsonCommonsFields.id)), documentIdField);
		}
		return validCopyIds;
	}

	/**
	 * Deletes, by query and {@link #PAGE_SIZE} ids at a time, the documents of the index with the given ids whose
	 * {@code timestamp} is not after the limit.
	 * @param entityName the index
	 * @param ids the ids of the documents
	 * @param timestampLimit the latest {@code timestamp} deleted ({@code Long.MAX_VALUE} for any)
	 * @return how many documents were deleted
	 */
	private static long deleteByIds(String entityName, List<String> ids, long timestampLimit) {
		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		String idFieldName = dbRequester.getFieldNameToId();
		CcpFieldName idField = new CcpFieldName(idFieldName);
		String timestampField = JnJsonCommonsFields.timestamp.getValue();
		long deleted = 0;
		int idsCount = ids.size();
		for (int start = 0; start < idsCount; start += PAGE_SIZE) {
			int end = Math.min(start + PAGE_SIZE, idsCount);
			List<String> page = new ArrayList<>(ids.subList(start, end));
			CcpQueryOptions documentsOfThePage = timestampLimit == Long.MAX_VALUE ? getDocumentsByIds(idField, page) : getExpiredDocumentsByIds(idField, page, timestampField, timestampLimit);
			CcpQueryExecutorDecorator documentsToDelete = documentsOfThePage.selectFrom(entityName);
			CcpJsonRepresentation response;
			try {
				response = documentsToDelete.delete();
			} catch (CcpErrorHttpClient e) {
				Integer status = e.entity.getAsIntegerNumber(Fields.status);
				boolean anotherError = NOT_FOUND != status;

				if(anotherError) {
					throw e;
				}

				// the index no longer exists (an entity removed or renamed): its documents are gone already
				return deleted;
			}
			boolean noCounter = false == response.containsAllFields(Fields.deleted);

			if(noCounter) {
				continue;
			}

			Long deletedOfThePage = response.getAsLongNumber(Fields.deleted);
			deleted += deletedOfThePage;
		}
		return deleted;
	}

	/**
	 * The documents with the given ids.
	 * @param idField the id field of the database
	 * @param ids the ids
	 * @return the query
	 */
	private static CcpQueryOptions getDocumentsByIds(CcpFieldName idField, List<String> ids) {
		CcpQueryOptions documents = CcpQueryOptions.INSTANCE
				.startQuery()
					.startBool()
						.startMust()
							.terms(idField, ids)
						.endMustAndBackToBool()
					.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		return documents;
	}

	/**
	 * The documents with the given ids whose {@code timestamp} is not after the limit.
	 * @param idField the id field of the database
	 * @param ids the ids
	 * @param timestampField the name of the {@code timestamp} field
	 * @param timestampLimit the latest {@code timestamp}
	 * @return the query
	 */
	private static CcpQueryOptions getExpiredDocumentsByIds(CcpFieldName idField, List<String> ids, String timestampField, long timestampLimit) {
		CcpQueryOptions documents = CcpQueryOptions.INSTANCE
				.startQuery()
					.startBool()
						.startMust()
							.terms(idField, ids)
							.startRange()
								.startFieldRange(timestampField)
									.lessThanEquals(timestampLimit)
								.endFieldRangeAndBackToRange()
							.endRangeAndBackToMust()
						.endMustAndBackToBool()
					.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		return documents;
	}
}
