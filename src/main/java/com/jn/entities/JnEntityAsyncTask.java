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
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidator;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.enums.CcpJsonFieldType;
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
		@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
		@CcpJsonFieldTypeNumber
		started(false), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
		@CcpJsonFieldTypeNumber
		finished(false), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.Number)
		@CcpJsonFieldTypeNumber
		enlapsedTime(false), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		data(false),
		@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		topic(false), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
		@CcpJsonFieldTypeNestedJson
		request(false), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		messageId(true), 
		@CcpJsonFieldValidator(type = CcpJsonFieldType.Boolean)
		success(false),
		@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		operationType(false),
		@CcpJsonFieldValidator(type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		operation(false),
		@CcpJsonFieldValidator(type = CcpJsonFieldType.NestedJson)
		@CcpJsonFieldTypeNestedJson
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
