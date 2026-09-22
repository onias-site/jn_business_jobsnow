package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnMustNotSendMessage;

@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityMessageDidNotSent.Fields.class)
@CcpEntityCache(3600)

public class JnEntityMessageDidNotSent implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityMessageDidNotSent.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		
		/**
		 * Nome da entidade cuja pesquisa no union-all impediu o envio, tal como gravado por
		 * {@code JnMustNotSendMessage} e pesquisado por {@code JnServiceLogin}.
		 */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		reasonType,

		/**
		 * Item de {@link JnMustNotSendMessage} — a lista de entidades a que o {@code reasonType} pertence.
		 */
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMustNotSendMessage.class)
		@CcpJsonFieldValidatorRequired
		reasonDescription,
		
		
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = JnReasonDetails.class)
		reasonDetails,
		
		@CcpJsonFieldTypeString
		reasonMessage,
	}
	
	public static enum JnReasonDetails{
		isNotPresentInThisUnionAll,
		missingFieldsToPrimaryKey,
		isPresentInThisUnionAll,
	}
	

}
