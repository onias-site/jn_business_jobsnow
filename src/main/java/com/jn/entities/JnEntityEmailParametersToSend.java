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

/**
 * Settings for sending each kind of e-mail: sender, template id, subject type and extra parameters. Seeded with the settings of the error notification and of the login token e-mail.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_email_parameters_to_send}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityEmailParametersToSend.Fields.class)
public class JnEntityEmailParametersToSend implements CcpEntityConfigurator{

	/** The entity {@code jn_email_parameters_to_send}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailParametersToSend.class).entityInstance;
 
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: validated as in {@code JnJsonCommonsFields}, transformed by {@code JnJsonTransformersFieldsEntityDoNothing}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
		email, 
		/** The {@code sender} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		sender, 
		/** The {@code templateId} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId, 
		/** The {@code subjectType} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		/** The {@code moreParameters} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		moreParameters, 
		/** The {@code contentType} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType
		;
	}

	/**
	 * Seeds the e-mail settings of the error notification to the developers and of the login token e-mail (with the links
	 * and names used by its template).
	 * @return the seed records
	 */
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
