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

/**
 * Handler de bulk que resolve conflito de sessão: se um registro de conflito existir
 * para o email, gera bulk item para deletá-lo; caso não exista, não faz nada.
 */
public class JnBulkHandlerSolveLoginConflict implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{
	enum JsonFieldNames implements CcpJsonFieldName{
		email
	}

	private JnBulkHandlerSolveLoginConflict() {}
	
	public static final JnBulkHandlerSolveLoginConflict INSTANCE = new JnBulkHandlerSolveLoginConflict();
	
	/**
	 * Gera item bulk de exclusão do conflito de sessão existente.
	 */
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {

		var deleteLoginConflict = JnEntityLoginSessionConflict.ENTITY.toBulkItems(json, CcpBulkEntityOperationType.delete);
		var asList = new ArrayList<CcpBulkItem>();
		asList.addAll(deleteLoginConflict);
		return asList;
	}

	/**
	 * Retorna lista vazia (sem conflito a resolver).
	 */
	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		return new ArrayList<>();
	}

	/**
	 * Retorna JnEntityLoginSessionConflict.ENTITY.
	 */
	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
