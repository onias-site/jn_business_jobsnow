package com.jn.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginSessionConflict;

/**
 * Bulk handler that solves a session conflict: if a conflict record exists for the
 * email, it produces a bulk item to delete it; otherwise, it does nothing.
 */
public class JnBulkHandlerSolveLoginConflict implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{
	/** Singleton; use {@link #INSTANCE}. */
	private JnBulkHandlerSolveLoginConflict() {}

	/** The single instance. */
	public static final JnBulkHandlerSolveLoginConflict INSTANCE = new JnBulkHandlerSolveLoginConflict();

	/**
	 * Produces the bulk item that deletes the existing session conflict.
	 * @param json the session
	 * @param recordFound the session conflict
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		var deleteLoginConflict = JnEntityLoginSessionConflict.ENTITY.toBulkItems(json, CcpBulkEntityOperationType.delete);
		var bulkItems = new ArrayList<CcpBulkItem>();
		bulkItems.addAll(deleteLoginConflict);
		return bulkItems;
	}

	/**
	 * Returns no item: there is no conflict to solve.
	 * @param json the session
	 * @return an empty list
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		return new ArrayList<>();
	}

	/**
	 * The entity searched: {@code jn_login_session_conflict}.
	 * @return the entity
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
