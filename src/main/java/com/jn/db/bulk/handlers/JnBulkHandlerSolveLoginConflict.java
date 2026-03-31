package com.jn.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginSessionConflict;

public class JnBulkHandlerSolveLoginConflict implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{
	enum JsonFieldNames implements CcpJsonFieldName{
		email
	}

	private JnBulkHandlerSolveLoginConflict() {}
	
	public static final JnBulkHandlerSolveLoginConflict INSTANCE = new JnBulkHandlerSolveLoginConflict();
	
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {
	
		var deleteLoginConflict = JnEntityLoginSessionConflict.ENTITY.getEntityDetails().getBulkItemsList(json, CcpBulkEntityOperationType.delete);
		var asList = new ArrayList<CcpBulkItem>();
		asList.addAll(deleteLoginConflict);
		return asList;
	}

	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		return new ArrayList<>();
	}

	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
