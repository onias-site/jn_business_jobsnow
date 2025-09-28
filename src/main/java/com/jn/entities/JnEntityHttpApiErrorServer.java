package com.jn.entities;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityExpurgable;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityOperationSpecification;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityTransferOperationEspecification;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidator;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.ccp.json.validations.fields.enums.CcpJsonFieldType;
import com.jn.entities.decorators.JnEntityExpurgable;
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;

@CcpEntityExpurgable(expurgTime = CcpEntityExpurgableOptions.hourly, expurgableEntityFactory = JnEntityExpurgable.class)
@CcpEntitySpecifications(
		classWithFieldsValidationsRules = JnEntityHttpApiErrorServer.Fields.class,
		inactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		reactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		delete = @CcpEntityOperationSpecification(afterOperation = {}),
	    save = @CcpEntityOperationSpecification(afterOperation = {}),
		cacheableEntity = true
)
//DOUBT ADICIONAR DATE?
public class JnEntityHttpApiErrorServer implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityHttpApiErrorServer.class).entityInstance;

	
	public static enum Fields implements CcpEntityField{
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 500)
		url(true), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
		method(true), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.NestedJson)
		@CcpJsonFieldTypeNestedJson
		headers(true), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		request(false), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 10)
		apiName(true),
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		details(true), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 500)
		response(false), 
		@CcpJsonFieldValidator(required = true, type = CcpJsonFieldType.String)
		@CcpJsonFieldTypeString(minLength = 1)
		status(false),
		timestamp(false)
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
