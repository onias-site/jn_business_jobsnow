package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityExpurgable;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityFieldTransformer;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.JnExpurgableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityTokenHash;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityExpurgable(expurgTime = CcpEntityExpurgableOptions.hourly, expurgableEntityFactory = JnExpurgableEntity.class)
@CcpEntitySpecifications(
		entityFieldsTransformers = JnJsonTransformersFieldsEntityDefault.class,
		entityValidation = JnEntityInstantMessengerMessageSent.Fields.class,
		cacheableEntity = true, 
		afterDeleteRecord = {},
		beforeSaveRecord = {},
		afterSaveRecord = {},
		flow = {}
)
public class JnEntityInstantMessengerMessageSent implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerMessageSent.class).entityInstance;

	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityTokenHash.class)
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		token, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		recipient, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 10000)
		interval
		;
	}

}
