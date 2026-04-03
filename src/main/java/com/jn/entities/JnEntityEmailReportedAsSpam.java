package com.jn.entities;

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
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityEmailReportedAsSpam.Fields.class)
public class JnEntityEmailReportedAsSpam implements CcpEntityConfigurator {
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailReportedAsSpam.class).entityInstance;
	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subject, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		sender
		;
	}

}
