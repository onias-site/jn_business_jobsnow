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
import com.jn.entities.JnEntityLoginTokenRequestResend;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;

/**
 * Bulk handler responsible for registering the user's login. Whether or not there is an
 * existing session conflict, it creates bulk items to save a new record in
 * JnEntityLoginSessionConflict and in JnEntityLoginSessionValidation.
 */
public class JnBulkHandlerRegisterLogin implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	
	private JnBulkHandlerRegisterLogin() {}
	
	public static final JnBulkHandlerRegisterLogin INSTANCE = new JnBulkHandlerRegisterLogin();
	
	/**
	 * Returns the bulk items to create the session and the conflict when a record already existed.
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	private List<CcpBulkItem> getBulkItems(CcpJsonRepresentation json) {
		CcpJsonRepresentation session = JnEntityLoginSessionConflict.ENTITY.getHandledJson(json);
		var newSession = JnEntityLoginSessionConflict.ENTITY.toBulkItems(session, CcpBulkEntityOperationType.create);
		CcpJsonRepresentation jsonCopy = new CcpJsonRepresentation(json.content);
		CcpJsonRepresentation login = JnEntityLoginSessionValidation.ENTITY.getHandledJson(jsonCopy);
		var newLogin = JnEntityLoginSessionValidation.ENTITY.toBulkItems(login, CcpBulkEntityOperationType.create);

		JnEntityLoginTokenRequestResend.ENTITY.getHandledJson(json);
		var allBulkItems = new ArrayList<CcpBulkItem>();
		allBulkItems.addAll(newSession);
		allBulkItems.addAll(newLogin);
		List<CcpBulkItem> otherBulkItems = this.getOtherBulkItems(json, CcpBulkEntityOperationType.delete, JnEntityLoginTokenRequestResend.ENTITY, JnEntityLoginTokenRequestUnlock.ENTITY);
		allBulkItems.addAll(otherBulkItems);
		return allBulkItems;
	} 
	
	private List<CcpBulkItem> getOtherBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, CcpEntity... entities){
		List<CcpBulkItem> response = new ArrayList<CcpBulkItem>();
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			response.addAll(bulkItems);
		}
		return response;
	}

	/**
	 * Returns the same bulk items when there was no previous record.
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * Returns JnEntityLoginSessionConflict.ENTITY as the entity to search.
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
