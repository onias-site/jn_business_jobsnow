package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntitySpecifications(
		entityFieldsTransformers = JnJsonTransformersFieldsEntityDefault.class,
		entityValidation = JnEntityAsyncTask.Fields.class,
		cacheableEntity = false, 
		afterDeleteRecord = {},
		beforeSaveRecord = {},
		afterSaveRecord = {},
		flow = {}
)
public class JnEntityAsyncTask implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityAsyncTask.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		started, 
		@CcpJsonFieldTypeNumberUnsigned
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
