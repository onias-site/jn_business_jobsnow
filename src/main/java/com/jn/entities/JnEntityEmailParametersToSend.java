package com.jn.entities;

import com.jn.entities.decorators.annotations.JnEntityVersionable;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.builders.JnEntityVersionableBuilder;
import com.jn.entities.decorators.builders.JnEntityVersionablePurgeBuilder;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDoNothing;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityEmailParametersToSend.Fields.class)
/**
 * Stores configuration parameters for sending emails: sender, templateId, subject type
 * and additional parameters. Versionable, 1-hour cache. Has initial records for the
 * error notification ({@code JnBusinessNotifyError}) and login token sending
 * ({@code JnNotifyUserAboutLoginToken}) contexts.
 */
public class JnEntityEmailParametersToSend implements CcpEntityConfigurator{

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailParametersToSend.class).entityInstance;
 
	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
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
		moreParameters, 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType
		;
	}

	public List<CcpBulkItem> getFirstRecordsToInsert() {
		String errorParametersWithEmail = "{" + "	\"email\": \"devs.jobsnow@gmail.com\",";
		String errorParametersWithSender = errorParametersWithEmail + "	\"sender\": \"devs.jobsnow@gmail.com\",";
		String errorParametersWithSubjectTypeKey = errorParametersWithSender
						+ "	\"subjectType\": \"";
						String errorSubjectType = JnMessages.JnNotifySupportAboutAnError.class.getName();
						String errorParametersWithSubjectTypeValue = errorParametersWithSubjectTypeKey
						+ errorSubjectType;
						String errorParametersWithSubjectType = errorParametersWithSubjectTypeValue		
						+ "\",";
						String errorParametersWithTemplateIdKey = errorParametersWithSubjectType + "	\"templateId\": \"";
						String errorTemplateId = JnMessages.JnNotifySupportAboutAnError.class.getName();
						String errorParametersWithTemplateIdValue = errorParametersWithTemplateIdKey
						+ errorTemplateId;
						String errorParametersWithTemplateId = errorParametersWithTemplateIdValue		
						+ "\"";
						String errorParameters = errorParametersWithTemplateId + "}";
						String tokenParametersWithSubjectTypeKey = "{\"sender\": \"devs.jobsnow@gmail.com\"," + "	\"subjectType\": \"";
						String tokenSubjectType = JnMessages.JnNotifyUserAboutLoginToken.class.getName();
						String tokenParametersWithSubjectTypeValue = tokenParametersWithSubjectTypeKey
						+ tokenSubjectType;
						String tokenParametersWithSubjectType = tokenParametersWithSubjectTypeValue
						+ "\",";
						String tokenParametersWithTemplateIdKey = tokenParametersWithSubjectType
						+ "	\"templateId\": \"";
						String tokenTemplateId = JnMessages.JnNotifyUserAboutLoginToken.class.getName();
						String tokenParametersWithTemplateIdValue = tokenParametersWithTemplateIdKey
						+ tokenTemplateId;
						String tokenParametersWithTemplateId = tokenParametersWithTemplateIdValue
						+ "\",";
						String tokenParametersWithMoreParametersKey = tokenParametersWithTemplateId + "	\"moreParameters\": {";
						String tokenParametersWithLinkedinAddress = tokenParametersWithMoreParametersKey
						+ "		\"linkedinAddress\": \"https://www.linkedin.com/in/onias85/\",";
						String tokenParametersWithLinkedinName = tokenParametersWithLinkedinAddress
						+ "		\"linkedinName\": \"Onias\",";
						String tokenParametersWithAccessLink = tokenParametersWithLinkedinName
						+ "		\"accessLink\": \"https://ccpjobsnow.com/#/tokenToSetPassword?email={email}&msgType=info&msgValue=newUser&token={token}\",";
						String tokenParametersWithTelegramGroupLink = tokenParametersWithAccessLink
						+ "		\"telegramGroupLink\": \"https://t.me/joinchat/q_PRgF_18n00NjEx\",";
						String tokenParametersWithBotAddress = tokenParametersWithTelegramGroupLink
						+ "		\"botAddress\": \"https://t.me/JnSuporteBot\"";
						String tokenParametersWithMoreParameters = tokenParametersWithBotAddress + "	}";
						String tokenParameters = tokenParametersWithMoreParameters + "}";
						List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, 
						errorParameters
						,
						tokenParameters
				
				)
				;

		return createBulkItems;
	}
}
