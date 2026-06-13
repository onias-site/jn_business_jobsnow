package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityAsyncTask.Fields.class)
/**
 * Representa uma tarefa assíncrona disparada via mensageria. Registra o ciclo de vida
 * da tarefa: início (started), fim (finished), tempo decorrido (enlapsedTime), dados,
 * tópico, request original, id da mensagem no PubSub, se foi bem-sucedido e qual
 * operação foi executada.
 */
public class JnEntityAsyncTask implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityAsyncTask.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumber
		started, 
		@CcpJsonFieldTypeNumber
		finished, 
		@CcpJsonFieldTypeNumberUnsigned
		enlapsedTime, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		data,
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		topic, 
		@CcpJsonFieldValidatorRequired
		request, 
		@CcpJsonFieldTypeString
		@CcpEntityFieldPrimaryKey
		messageId, 
		@CcpJsonFieldTypeBoolean
		success,
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		operation,
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		response
		;
		
	}
}
