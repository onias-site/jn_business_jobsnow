package com.jn.entities;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityOperationSpecification;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityTransferOperationEspecification;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.CcpJsonCommonsFields;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberNatural;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;

@CcpEntitySpecifications(
		classWithFieldsValidationsRules = JnEntityAsyncTask.Fields.class,
		inactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		reactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		delete = @CcpEntityOperationSpecification(afterOperation = {}),
	    save = @CcpEntityOperationSpecification(afterOperation = {}),
		cacheableEntity = false
)
public class JnEntityAsyncTask implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityAsyncTask.class).entityInstance;

	public static enum Fields implements CcpEntityField{
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberNatural
		started(false), 
		@CcpJsonFieldTypeNumberNatural
		finished(false), 
		@CcpJsonFieldTypeNumberNatural
		enlapsedTime(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		data(false),
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		topic(false), 
		@CcpJsonFieldValidatorRequired
		request(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		messageId(true), 
		@CcpJsonFieldTypeBoolean
		success(false),
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		operationType(false),
		@CcpJsonCommonsFields(JnJsonCommonsFields.class)
		operation(false),
		@CcpJsonCommonsFields(JnJsonCommonsFields.class)
		response(false)
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
