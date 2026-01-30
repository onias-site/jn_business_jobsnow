package com.jn.db.bulk;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.jn.entities.JnEntityRecordToReprocess;
class FunctionReprocessMapper implements Function<CcpBulkOperationResult, CcpJsonRepresentation>{
	enum JsonFieldNames implements CcpJsonFieldName{
		type
	}

	public static final FunctionReprocessMapper INSTANCE = new FunctionReprocessMapper();
	
	private FunctionReprocessMapper() {}

	public CcpJsonRepresentation apply(CcpBulkOperationResult result) {
		long currentTimeMillis = System.currentTimeMillis();
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityRecordToReprocess.Fields.timestamp, currentTimeMillis);
		CcpBulkItem bulkItem = result.getBulkItem();
		CcpJsonRepresentation putAll = put.mergeWithAnotherJson(bulkItem.json);
		CcpJsonRepresentation errorDetails = result.getErrorDetails();
		CcpJsonRepresentation putAll2 = putAll.mergeWithAnotherJson(errorDetails);
		CcpJsonRepresentation renameKey = putAll2.renameField(JsonFieldNames.type, JnEntityRecordToReprocess.Fields.errorType);
		CcpJsonRepresentation jsonPiece = renameKey.getJsonPiece( JnEntityRecordToReprocess.Fields.errorType,  JnEntityRecordToReprocess.Fields.reason);
		return jsonPiece;
	} 
	
	
	
}
