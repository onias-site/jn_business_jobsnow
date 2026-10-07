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
 * Bulk handler that registers the login: whether or not there was a session conflict, it creates the session conflict
 * record and the session record, and deletes the pending requests to resend and to unlock the login token.
 */
public class JnBulkHandlerRegisterLogin implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	
	/** Singleton; use {@link #INSTANCE}. */
	private JnBulkHandlerRegisterLogin() {}
	
	/** The single instance. */
	public static final JnBulkHandlerRegisterLogin INSTANCE = new JnBulkHandlerRegisterLogin();
	
	/**
	 * Returns the items that register the login.
	 * @param json the login
	 * @param recordFound the existing session conflict
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * Builds the items that create the session conflict and the session, and delete the pending token requests.
	 * @param json the login
	 * @return the bulk items
	 */
	private List<CcpBulkItem> getBulkItems(CcpJsonRepresentation json) {
		CcpJsonRepresentation session = JnEntityLoginSessionConflict.ENTITY.getHandledJson(json);
		var newSession = JnEntityLoginSessionConflict.ENTITY.toBulkItems(session, CcpBulkEntityOperationType.create);
		CcpJsonRepresentation jsonCopy = new CcpJsonRepresentation(json.content);
		CcpJsonRepresentation login = JnEntityLoginSessionValidation.ENTITY.getHandledJson(jsonCopy);
		var newLogin = JnEntityLoginSessionValidation.ENTITY.toBulkItems(login, CcpBulkEntityOperationType.create);

		var allBulkItems = new ArrayList<CcpBulkItem>();
		allBulkItems.addAll(newSession);
		allBulkItems.addAll(newLogin);
		List<CcpBulkItem> otherBulkItems = this.getOtherBulkItems(json, CcpBulkEntityOperationType.delete, JnEntityLoginTokenRequestResend.ENTITY, JnEntityLoginTokenRequestUnlock.ENTITY);
		allBulkItems.addAll(otherBulkItems);
		return allBulkItems;
	} 
	
	/**
	 * Builds the items of the operation in each entity.
	 * @param json the record
	 * @param operation the bulk operation
	 * @param entities the entities
	 * @return the bulk items
	 */
	private List<CcpBulkItem> getOtherBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation, CcpEntity... entities){
		List<CcpBulkItem> response = new ArrayList<CcpBulkItem>();
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			response.addAll(bulkItems);
		}
		return response;
	}

	/**
	 * Returns the items that register the login.
	 * @param json the login
	 * @return the bulk items
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * The entity searched: {@code jn_login_session_conflict}.
	 * @return the entity
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
