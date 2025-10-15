package com.jn.entities;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.decorators.annotations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.entities.decorators.JnVersionableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntitySpecifications(
		entityFieldsTransformers = JnJsonTransformersFieldsEntityDefault.class,
		entityValidation = JnEntityEmailParametersToSend.Fields.class,
		cacheableEntity = true, 
		afterDeleteRecord = {},
		beforeSaveRecord = {},
		afterSaveRecord = {},
		flow = {}
)
public class JnEntityEmailParametersToSend  implements CcpEntityConfigurator{

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailParametersToSend.class).entityInstance;
 
	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		sender, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		moreParameters
		;
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
