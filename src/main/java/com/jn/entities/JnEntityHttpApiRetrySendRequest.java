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
import com.jn.entities.decorators.JnEntityExpurgable;
import com.jn.json.fields.validation.JnJsonValidationsByFieldName;
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;

@CcpEntityExpurgable(expurgTime = CcpEntityExpurgableOptions.hourly, expurgableEntityFactory = JnEntityExpurgable.class)
@CcpEntitySpecifications(
		classWithFieldsValidationsRules = JnEntityHttpApiRetrySendRequest.Fields.class,
		inactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		reactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		delete = @CcpEntityOperationSpecification(afterOperation = {}),
	    save = @CcpEntityOperationSpecification(afterOperation = {}),
		cacheableEntity = true
)

public class JnEntityHttpApiRetrySendRequest implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityHttpApiRetrySendRequest.class).entityInstance;
	
	public static boolean exceededTries(CcpJsonRepresentation json, String fieldName, int limit) {
		
		for(int k = 1; k <= limit; k++) {
			
			CcpJsonRepresentation put = json.getDynamicVersion().put(fieldName, k);
			
			boolean exists = ENTITY.exists(put);
			
			if(exists == false) {
				ENTITY.createOrUpdate(put);
				return false;
			}
		}
		return true;
	}

	public static enum Fields implements CcpEntityField{
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		url(true), 
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		method(true), 
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		headers(true), 
		@CcpJsonFieldValidator(validationsCatalog = {JnJsonValidationsByFieldName.class})
		request(false), 
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		apiName(true), 
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		attempts(true), 
		@CcpJsonFieldValidator(validationsCatalog = {JnJsonValidationsByFieldName.class})
		response(false), 
		@CcpJsonFieldValidator(validationsCatalog = {JnJsonValidationsByFieldName.class})
		httpStatus(false),
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		timestamp(false),
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		date(false),
		@CcpJsonFieldValidator(required = true, validationsCatalog = {JnJsonValidationsByFieldName.class})
		details(false)
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
