package com.jn.db.bulk;

import java.util.function.Function;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Função de mapeamento usada pelo JnExecuteBulkOperation para converter um resultado
 * de operação bulk com erro em um registro de reprocessamento (JnEntityRecordToReprocess).
 * Previne loops infinitos ao rejeitar itens que já pertencem à entidade de reprocessamento.
 */
class FunctionReprocessMapper implements Function<CcpBulkOperationResult, CcpJsonRepresentation>{
	enum JsonFieldNames implements CcpJsonFieldName{
		type
	}

	public static final FunctionReprocessMapper INSTANCE = new FunctionReprocessMapper();
	
	private FunctionReprocessMapper() {}

	/**
	 * Extrai detalhes do item bulk com erro, adiciona timestamp atual, renomeia o campo
	 * type para errorType e monta o JSON no formato de JnEntityRecordToReprocess.
	 * Lança RuntimeException se o item for da própria entidade de reprocessamento
	 * (prevenção de loop).
	 */
	public CcpJsonRepresentation apply(CcpBulkOperationResult result) {
		CcpBulkItem bulkItem = result.getBulkItem();
		CcpEntityMetaData entityDetails = bulkItem.entity.getEntityMetaData();
		CcpEntityMetaData entityMetaData = JnEntityRecordToReprocess.ENTITY.getEntityMetaData();
		boolean itIsTryingToStartAnInfinitLoop = entityDetails.entityName.equals(entityMetaData.entityName);
		if(itIsTryingToStartAnInfinitLoop) {
			JnErrorReprocessInfiniteLoopPrevented jnErrorReprocessInfiniteLoopPrevented = new JnErrorReprocessInfiniteLoopPrevented();
			throw jnErrorReprocessInfiniteLoopPrevented;
		}
		long currentTimeMillis = System.currentTimeMillis();
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, currentTimeMillis);
		CcpJsonRepresentation putAll = put.mergeWithAnotherJson(bulkItem.json);
		CcpJsonRepresentation errorDetails = result.getErrorDetails();
		CcpJsonRepresentation putAll2 = putAll.mergeWithAnotherJson(errorDetails);
		CcpJsonRepresentation renameKey = putAll2.renameField(JsonFieldNames.type, JnEntityRecordToReprocess.Fields.errorType);
		CcpJsonRepresentation put2 = renameKey.put(JnJsonCommonsFields.id, bulkItem.id);
		CcpJsonRepresentation put3 = put2.put(JnJsonCommonsFields.entity, entityDetails.entityName);
		var fieldsValues = JnEntityRecordToReprocess.Fields.values();
		CcpJsonRepresentation jsonPiece = put3
		.getJsonPiece(fieldsValues);
		return jsonPiece;
	}

	@SuppressWarnings("serial")
	private static class JnErrorReprocessInfiniteLoopPrevented extends RuntimeException {
	}
}
