package com.jn.db.bulk.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpEntityBulkOperationType;
import com.ccp.especifications.db.crud.CcpHandleWithSearchResultsInTheEntity;
import com.ccp.especifications.db.utils.CcpEntity;
import com.jn.entities.JnEntityLoginConflict;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginSessionValidation;

enum JnBulkHandlerSolveLoginConflictConstants  implements CcpJsonFieldName{
	email
}

public class JnBulkHandlerSolveLoginConflict implements CcpHandleWithSearchResultsInTheEntity<List<CcpBulkItem>>{

	private JnBulkHandlerSolveLoginConflict() {}
	
	public static final JnBulkHandlerSolveLoginConflict INSTANCE = new JnBulkHandlerSolveLoginConflict();
	
	public List<CcpBulkItem> whenRecordWasFoundInTheEntitySearch(CcpJsonRepresentation json, CcpJsonRepresentation recordFound) {
	
		String email = recordFound.getAsString(JnBulkHandlerSolveLoginConflictConstants.email);
		
		CcpJsonRepresentation newLogin = JnEntityLoginSessionValidation.ENTITY.getOnlyExistingFields(json);
		
		CcpJsonRepresentation loginConflict = CcpOtherConstants.EMPTY_JSON
				.put(JnBulkHandlerSolveLoginConflictConstants.email, email)
				.put(JnEntityLoginConflict.Fields.oldLogin, recordFound)
				.put(JnEntityLoginConflict.Fields.newLogin, newLogin)
				;
		
		CcpBulkItem itemLoginLoginConflict = JnEntityLoginConflict.ENTITY.getMainBulkItem(loginConflict, CcpEntityBulkOperationType.create);
		CcpBulkItem deleteLoginConflict = JnEntityLoginSessionConflict.ENTITY.getMainBulkItem(json, CcpEntityBulkOperationType.delete);
		List<CcpBulkItem> asList = Arrays.asList(itemLoginLoginConflict, deleteLoginConflict);
		return asList;
	}

	public List<CcpBulkItem> whenRecordWasNotFoundInTheEntitySearch(CcpJsonRepresentation json) {
		return new ArrayList<>();
	}

	public CcpEntity getEntityToSearch() {
		return JnEntityLoginSessionConflict.ENTITY;
	}

}
