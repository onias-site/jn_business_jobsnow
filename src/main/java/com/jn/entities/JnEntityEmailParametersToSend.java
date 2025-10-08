package com.jn.entities;

import java.util.List;
import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityDecorators;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityOperationSpecification;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityTransferOperationEspecification;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.entities.decorators.JnEntityVersionable;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.transformers.JnJsonTransformersDefaultEntityFields;

@CcpEntityDecorators(decorators = JnEntityVersionable.class)
@CcpEntitySpecifications(
		classWithFieldsValidationsRules = JnEntityEmailParametersToSend.Fields.class,
		inactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		reactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		delete = @CcpEntityOperationSpecification(afterOperation = {}),
	    save = @CcpEntityOperationSpecification(afterOperation = {}),
		cacheableEntity = true
)
public class JnEntityEmailParametersToSend  implements CcpEntityConfigurator{

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailParametersToSend.class).entityInstance;
 
	public static enum Fields implements CcpEntityField {
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		sender(false), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId(true), 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType(false), 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		moreParameters(false)
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

	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, 
				"{" + "	\"email\": \"devs.jobsnow@gmail.com\"," + "	\"sender\": \"devs.jobsnow@gmail.com\","
						+ "	\"subjectType\": \""
						+ JnBusinessNotifyError.class.getName()		
						+ "\"," + "	\"templateId\": \""
						+ JnBusinessNotifyError.class.getName()		
						+ "\"" + "}",
				"{" + "	\"sender\": \"devs.jobsnow@gmail.com\"," + "	\"subjectType\": \""
						+ JnBusinessSendUserToken.class.getName()
						+ "\","
						+ "	\"templateId\": \""
						+ JnBusinessSendUserToken.class.getName()
						+ "\"," + "	\"moreParameters\": {"
						+ "		\"linkedinAddress\": \"https://www.linkedin.com/in/onias85/\","
						+ "		\"linkedinName\": \"Onias\","
						+ "		\"accessLink\": \"https://ccpjobsnow.com/#/tokenToSetPassword?email={email}&msgType=info&msgValue=newUser&token={token}\","
						+ "		\"telegramGroupLink\": \"https://t.me/joinchat/q_PRgF_18n00NjEx\","
						+ "		\"botAddress\": \"https://t.me/JnSuporteBot\"" + "	}" + "}");

		return createBulkItems;
	}
}
