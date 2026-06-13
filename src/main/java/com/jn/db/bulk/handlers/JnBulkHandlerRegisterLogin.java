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

/**
 * Handler de bulk responsável por registrar o login do usuário. Independentemente de
 * haver ou não conflito de sessão existente, cria itens bulk para salvar um novo registro
 * em JnEntityLoginSessionConflict e em JnEntityLoginSessionValidation.
 */
public class JnBulkHandlerRegisterLogin implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	
	private JnBulkHandlerRegisterLogin() {}
	
	public static final JnBulkHandlerRegisterLogin INSTANCE = new JnBulkHandlerRegisterLogin();
	
	/**
	 * Retorna os bulk items para criação de sessão e conflito quando já havia um registro.
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	private List<CcpBulkItem> getBulkItems(CcpJsonRepresentation json) {
		CcpJsonRepresentation session = JnEntityLoginSessionConflict.ENTITY.getHandledJson(json);
		var newSession = JnEntityLoginSessionConflict.ENTITY.toBulkItems(session, CcpBulkEntityOperationType.create);
		CcpJsonRepresentation login = JnEntityLoginSessionValidation.ENTITY.getHandledJson(new CcpJsonRepresentation(json.content));
		var newLogin = JnEntityLoginSessionValidation.ENTITY.toBulkItems(login, CcpBulkEntityOperationType.create);
		var asList = new ArrayList<CcpBulkItem>();
		asList.addAll(newSession);
		asList.addAll(newLogin);
		return asList;
	} 

	/**
	 * Retorna os mesmos bulk items quando não havia registro anterior.
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.getBulkItems(json);
		return bulkItems;
	}

	/**
	 * Retorna JnEntityLoginSessionConflict.ENTITY como entidade de pesquisa.
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
