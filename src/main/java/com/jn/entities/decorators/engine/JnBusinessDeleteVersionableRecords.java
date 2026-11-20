package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnBusinessSendToMensageria;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnVersionableHistory;

/**
 * Asynchronous task that purges the history of a versionable entity record: the group of {@code jn_grouper} where
 * {@code JnVersionableEntity} keeps its versions ({@link JnVersionableHistory}), under each name of the record.
 *
 * <p>The record itself is not deleted here. The one that enqueues this task is {@code JnVersionablePurgeEntity}, which
 * has already delegated the real deletion down the chain. The history is deleted by bulk, not through the entity API,
 * so the deletion does not go through the versionable {@code toBulkItems} again and save one more version precisely in
 * the purge meant to wipe it.
 *
 * <p>The history is deleted under every name of the record, not only the main one: in a twin entity each transfer saves
 * a version on both ends, one under the main entity's name and another under the twin's. A group that does not exist is
 * deleted silently. Until 2026-11-20 the history was made of rows of {@code jn_versionable}, deleted by query.
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
	}

	/**
	 * Deletes the history of the record under each of its names.
	 * @param json {@code entitiesToDelete} (the entity names of the record) and {@code id} (its document id)
	 * @return {@code deleted}: how many histories were asked to be deleted (one per name)
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String[] entitiesToDelete = json.getAsStringArray(JsonFieldNames.entitiesToDelete);
		String recordId = json.getAsString(JnJsonCommonsFields.id);

		List<CcpBulkItem> itemsToDelete = new ArrayList<>();
		for (String entityName : entitiesToDelete) {
			CcpBulkItem itemToDelete = JnVersionableHistory.getItemToDeleteTheHistory(entityName, recordId);
			itemsToDelete.add(itemToDelete);
		}

		JnExecuteBulkOperation.INSTANCE.executeBulk(itemsToDelete, JnDeleteKeysFromCache.INSTANCE);

		int deleted = itemsToDelete.size();
		CcpJsonRepresentation result = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.deleted, deleted);

		return result;
	}
}
