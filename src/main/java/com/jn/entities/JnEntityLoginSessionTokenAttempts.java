package com.jn.entities;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityLoginSessionTokenAttempts.Fields.class)
/**
 * Contador de tentativas de uso de token de sessão inválido. Quando o limite é atingido, aciona
 * o bloqueio da senha do usuário. Possui métodos estáticos de fábrica para incrementar ou resetar
 * o contador. Cache de 1 hora.
 */
public class JnEntityLoginSessionTokenAttempts implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityLoginSessionTokenAttempts.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		attempts
		;
		
		
	}
	public static CcpBusiness incrementAttempts(Integer maxAttempts, CcpBusiness whenExceedAttempts) {
		CcpBusiness result = json -> {
			
			CcpJsonRepresentation record = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, ENTITY);
			Double attempts = record.getOrDefault(Fields.attempts, () -> 0d);
			Double updatedAttempts = attempts + 1;
			
			boolean excedeedAttempts = updatedAttempts >= maxAttempts;
			
			if(excedeedAttempts) {
				whenExceedAttempts.apply(json);
				return json;
			}
			
			CcpJsonRepresentation jsonPiece = json.getJsonPiece(Fields.email);
			CcpJsonRepresentation mergeWithAnotherJson = record.put(Fields.attempts, updatedAttempts.intValue()).mergeWithAnotherJson(jsonPiece);
			ENTITY.save(mergeWithAnotherJson);
			return json;
		};
		return result;
	}

	public static CcpBusiness resetAttempts() {
		CcpBusiness result = json -> {
			CcpJsonRepresentation record = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, ENTITY);
			
			boolean noAttemps = record.isEmpty();
			
			if(noAttemps) {
				return json;
			}
			
			ENTITY.delete(record);
			return json;
		};
		return result;
	}
}
