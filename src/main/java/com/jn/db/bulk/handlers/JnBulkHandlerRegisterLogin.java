package com.jn.db.bulk.handlers;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;

public class JnBulkHandlerRegisterLogin implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	
	private JnBulkHandlerRegisterLogin() {}
	
	public static final JnBulkHandlerRegisterLogin INSTANCE = new JnBulkHandlerRegisterLogin();
	
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}
	//FIXME LGPD VS EXPURGABLE
	private List<CcpBulkItem> getBulkItems(CcpJsonRepresentation json) {
		CcpJsonRepresentation sessionConflictLgpd = JnEntityLoginSessionConflict.ENTITY.getTransformedJsonByEachFieldInJson(json);
		var newSession = JnEntityLoginSessionConflict.ENTITY.toBulkItems(sessionConflictLgpd, CcpBulkEntityOperationType.create);
		CcpJsonRepresentation sessionValidationLgpd = JnEntityLoginSessionValidation.ENTITY.getTransformedJsonByEachFieldInJson(json);
		var newLogin = JnEntityLoginSessionValidation.ENTITY.toBulkItems(sessionValidationLgpd, CcpBulkEntityOperationType.create);
		var asList = new ArrayList<CcpBulkItem>();
		asList.addAll(newSession);
		asList.addAll(newLogin);
		return asList;
	}

	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
