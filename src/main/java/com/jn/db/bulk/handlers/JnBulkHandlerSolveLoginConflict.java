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
	private JnBulkHandlerSolveLoginConflict() {}

	public static final JnBulkHandlerSolveLoginConflict INSTANCE = new JnBulkHandlerSolveLoginConflict();

	/**
	 * Produces the bulk item that deletes the existing session conflict.
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		var deleteLoginConflict = JnEntityLoginSessionConflict.ENTITY.toBulkItems(json, CcpBulkEntityOperationType.delete);
		var bulkItems = new ArrayList<CcpBulkItem>();
		bulkItems.addAll(deleteLoginConflict);
		return bulkItems;
	}

	/**
	 * Returns an empty list (no conflict to solve).
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		return new ArrayList<>();
	}

	/**
	 * Returns JnEntityLoginSessionConflict.ENTITY.
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
