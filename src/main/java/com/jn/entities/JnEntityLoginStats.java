package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeBefore;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

@CcpEntitySpecifications(
		entityFieldsTransformers = JnJsonTransformersFieldsEntityDefault.class,
		entityValidation = JnEntityLoginStats.Fields.class,
		cacheableEntity = true, 
		afterDeleteRecord = {},
		beforeSaveRecord = {},
		afterSaveRecord = {},
		flow = {}
)
public class JnEntityLoginStats implements CcpEntityConfigurator {
	
	public static final CcpEntity INSTANCE = new CcpEntityFactory(JnEntityLoginStats.class).entityInstance;
	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 35, maxLength = 50)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumber(minValue = -1_000_000, maxValue = 1_000_000)
		balance, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.yearly, maxValue = 100, minValue = 0)
		lastAccess, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		countAccess,
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		openedTickets, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		closedTickets, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		balanceTransacionsCount
		;
	}
}
