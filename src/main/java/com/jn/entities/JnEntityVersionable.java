package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldNotUpdatable;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityOlyReadable
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityVersionable.Fields.class)

public class JnEntityVersionable implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityVersionable.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		operation,
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		date,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id,
		@CcpEntityFieldNotUpdatable
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		json
		;
	}
}
