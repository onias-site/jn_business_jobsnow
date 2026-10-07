package com.jn.entities.decorators.engine;

import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.query.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityVersionable;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnBusinessSendToMensageria;

/**
 * Asynchronous task that purges the history of a versionable entity record: every row that
 * {@code JnVersionableEntity} saved for it in {@code JnEntityVersionable}.
 *
 * <p>The record itself is not deleted here. The one that enqueues this task is
 * {@code JnVersionablePurgeEntity}, which has already delegated the real deletion down the chain. Until
 * 2026-09-27 the query also deleted the record by {@code _id}: with the real deletion happening
 * milliseconds earlier, the {@code _delete_by_query} search still saw the old version of the
 * document and Elasticsearch aborted with 409 (version conflict) — which stayed hidden while
 * {@code CcpHttpHandler} swallowed unexpected statuses.
 *
 * <p>The deletion is done by query, not through the entity API, for two reasons. The first is
 * volume: a single record accumulates one history row per operation ever performed on it, and the
 * primary key of those rows includes the operation {@code timestamp}, so there is no known set of
 * ids to delete, but rather a set identified by the pair ({@code entity}, {@code id}) —
 * exactly the pair that {@code JnVersionableEntity} saves in each row. The second is to avoid the
 * decorator's own side effect: deleting through the entity would go through the versionable
 * {@code toBulkItems} again and save one more history row precisely in the purge meant to wipe it.
 *
 * <p>The entity name is searched among all the record's tables, not only the main one: in a
 * twin entity each transfer saves history on both ends, one row under the main entity's name
 * and another under the twin's.
 */
public class JnBusinessDeleteVersionableRecords implements JnBusinessSendToMensageria {

	/** The single instance. */
	public static final JnBusinessDeleteVersionableRecords INSTANCE = new JnBusinessDeleteVersionableRecords();

	/** Singleton; use {@link #INSTANCE}. */
	private JnBusinessDeleteVersionableRecords() {}

	/** Fields of the purge request and of its result. */
	public static enum JsonFieldNames implements CcpJsonFieldName {
		/** The {@code entitiesToDelete} field. */
		entitiesToDelete,
		/** The {@code deleted} field. */
		deleted,
		/** The {@code version_conflicts} field of the {@code _delete_by_query} response. */
		version_conflicts
	}

	/** Rounds of the purge while rows keep being skipped by version conflicts. */
	static final int MAX_PURGE_ROUNDS = 3;

	/**
	 * Deletes, by query, every history row of the record. The deletion skips the rows changed while it runs (Elasticsearch
	 * reports them in {@code version_conflicts}) instead of failing, and is run again, up to {@value #MAX_PURGE_ROUNDS}
	 * rounds in all, while some row was skipped. Until 2026-10-06 one concurrent write on the history aborted the purge
	 * with 409.
	 * @param json {@code entitiesToDelete} (the entity names of the record) and {@code id} (its serialized primary key)
	 * @return {@code deleted}: how many rows were deleted, in all the rounds
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		CcpQueryOptions request = this.getRequestToDeleteTheHistory(json);

		CcpEntityMetaData versionableMetaData = JnEntityVersionable.ENTITY.getEntityMetaData();

		CcpQueryExecutorDecorator historyToDelete = request.selectFrom(versionableMetaData.entityName);

		long deleted = 0;

		for (int round = 1; round <= MAX_PURGE_ROUNDS; round++) {
			CcpJsonRepresentation response = historyToDelete.delete();
			deleted += this.getCount(response, JsonFieldNames.deleted);
			long versionConflicts = this.getCount(response, JsonFieldNames.version_conflicts);
			boolean nothingWasSkipped = versionConflicts == 0;
			if(nothingWasSkipped) {
				break;
			}
		}

		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.deleted, deleted);

		return result;
	}

	/**
	 * Reads a counter of the {@code _delete_by_query} response, 0 when absent.
	 * @param response the response
	 * @param field the counter
	 * @return the value
	 */
	private long getCount(CcpJsonRepresentation response, JsonFieldNames field) {
		boolean absent = false == response.containsAllFields(field);
		if(absent) {
			return 0;
		}
		Long count = response.getAsLongNumber(field);
		return count;
	}

	/**
	 * Finds the record's history by the pair every row stores: the source entity name in
	 * {@code entity} and the serialized primary key in {@code id}.
	 */
	private CcpQueryOptions getRequestToDeleteTheHistory(CcpJsonRepresentation json) {

		String[] entitiesToDelete = json.getAsStringArray(JsonFieldNames.entitiesToDelete);
		List<String> entitiesOfTheRecord = Arrays.asList(entitiesToDelete);
		String versionableRecordId = json.getAsString(JnJsonCommonsFields.id);

		var query = CcpQueryOptions.INSTANCE
				.startQuery();
				var bool = query
				.startBool();
				var mustWithTheEntityName = bool
				.startMust()
				.terms(JnJsonCommonsFields.entity, entitiesOfTheRecord);
				var mustWithTheRecordId = mustWithTheEntityName
				.term(JnJsonCommonsFields.id, versionableRecordId);
				var boolWithTheRecordId = mustWithTheRecordId
				.endMustAndBackToBool();
				var queryWithTheRecordId = boolWithTheRecordId
				.endBoolAndBackToQuery();

		CcpQueryOptions requestToDeleteTheHistory = queryWithTheRecordId
				.endQueryAndBackToRequest();

		return requestToDeleteTheHistory;
	}
}
