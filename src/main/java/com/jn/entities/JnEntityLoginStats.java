package com.jn.entities;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeTimeBefore;
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;

@CcpEntitySpecifications(
		jsonValidation = JnEntityLoginStats.Fields.class,
		cacheableEntity = true, 
		afterSaveRecord = {},
		afterDeleteRecord = {} 
)
public class JnEntityLoginStats implements CcpEntityConfigurator {
	
	public static final CcpEntity INSTANCE = new CcpEntityFactory(JnEntityLoginStats.class).entityInstance;
	
	public static enum Fields implements CcpEntityField{
		@CcpJsonFieldTypeString(minLength = 35, maxLength = 50)
		email(true), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumber(minValue = -1_000_000, maxValue = 1_000_000)
		balance(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeTimeBefore(intervalType = CcpEntityExpurgableOptions.yearly, maxValue = 100, minValue = 0)
		lastAccess(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		countAccess(false),
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		openedTickets(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		closedTickets(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned(maxValue = 1000)
		balanceTransacionsCount(false)
		;
		
		private final boolean primaryKey;

		private final Function<CcpJsonRepresentation, CcpJsonRepresentation> transformer;
		
		private Fields(boolean primaryKey) {
			this(primaryKey, CcpOtherConstants.DO_NOTHING);
		}

		private Fields(boolean primaryKey, Function<CcpJsonRepresentation, CcpJsonRepresentation> transformer) {
			this.transformer = transformer;
			this.primaryKey = primaryKey;
		}
		
		public Function<CcpJsonRepresentation, CcpJsonRepresentation> getTransformer() {
			return this.transformer == CcpOtherConstants.DO_NOTHING ? JnJsonTransformersDefaultEntityFields.getTransformer(this) : this.transformer;
		}
		public boolean isPrimaryKey() {
			return this.primaryKey;
		}

	}
}
