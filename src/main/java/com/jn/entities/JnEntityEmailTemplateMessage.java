package com.jn.entities;

import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionablePurge;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnEmailFooter;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;

/**
 * E-mail templates by language and template id; the {@code message} supports variables such as {@code {token}} and {@code {email}}, resolved at sending time. Seeded in Portuguese and English for the login token and the error notification.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_email_template_message}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in its history ({@code JnVersionableHistory}, in {@code jn_grouper})</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JnEntityEmailTemplateMessage.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityEmailTemplateMessage  implements CcpEntityConfigurator{
	/** The single instance: the class keeps no state. */
	public final static JnEntityEmailTemplateMessage INSTANCE = new JnEntityEmailTemplateMessage();

	private JnEntityEmailTemplateMessage() {}

	/** The entity {@code jn_email_template_message}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailTemplateMessage.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{ 
		/** The {@code templateId} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId,
		/** The {@code language} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language, 
		/** The {@code subject} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subject, 
		/** The {@code message} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		message
		;
	}

	/**
	 * Seeds, in Portuguese and English, the HTML template of the login token e-mail and the template of the error e-mail.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		String tokenTemplateWithLanguageKey = "{"
				+ "	\"language\": \"";
				String portugueseLanguage = JnLanguage.portuguese.name();
				String tokenTemplateWithLanguageValue = tokenTemplateWithLanguageKey + portugueseLanguage;
				String tokenTemplateWithLanguage = tokenTemplateWithLanguageValue
				+ "\",";
				String tokenTemplateWithTemplateIdKey = tokenTemplateWithLanguage
				+ "	\"templateId\": \"";
				String tokenTemplateId = JnMessages.JnNotifyUserAboutLoginToken.class.getName();
				String tokenTemplateWithTemplateIdValue = tokenTemplateWithTemplateIdKey + tokenTemplateId;
				String tokenTemplateWithTemplateId = tokenTemplateWithTemplateIdValue
				+ "\",";
				String tokenTemplateWithSubject = tokenTemplateWithTemplateId
				+ "	\"subject\": \"Envio de token para acesso ao Onias.Site\",";
				String tokenTemplateWithMessage = tokenTemplateWithSubject
				+ "	\"message\": \"<html><head><style>p,h4{ font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;}#customers {  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;  border-collapse: collapse;  width: 100%;}#customers td, #customers th {  border: 1px solid #ddd;  padding: 8px;}#customers tr:nth-child(even){background-color: #f2f2f2;}#customers tr:hover {background-color: #ddd;}#customers th {  padding-top: 12px;  padding-bottom: 12px;  text-align: left;  background-color: #7FBCEC;  color: white;}</style></head><body><div><p>Saudações, caso você não me conheça ou não se lembre de mim, sou o <a href='{linkedinAddress}' target = '_blank'>{linkedinName}</a></p><p>Estou estou enviando um token para acesso ao sistema <a href='{accessLink}' target = '_blank'>Onias.Site</a> esse token é {token} para você poder configurar seu usuário</p></div>" + JnEmailFooter.portuguese.getValue() + "</body></html>\"";
				String portugueseTokenTemplate = tokenTemplateWithMessage
				+ "}";
				String errorTemplateWithLanguageKey = "{"
				+ "	\"language\": \"";
				String portugueseLanguageForError = JnLanguage.portuguese.name();
				String errorTemplateWithLanguageValue = errorTemplateWithLanguageKey
				+ portugueseLanguageForError;
				String errorTemplateWithLanguage = errorTemplateWithLanguageValue
				+ "\",";
				String errorTemplateWithTemplateIdKey = errorTemplateWithLanguage
				+ "	\"templateId\": \"";
				String errorTemplateId = JnMessages.JnNotifySupportAboutAnError.class.getName();
				String errorTemplateWithTemplateIdValue = errorTemplateWithTemplateIdKey + errorTemplateId;
				String errorTemplateWithTemplateId = errorTemplateWithTemplateIdValue
				+ "\",";
				String errorTemplateWithSubject = errorTemplateWithTemplateId
				+ "	\"subject\": \"[ERROR] {type}\",";
				String errorTemplateWithMessage = errorTemplateWithSubject
				+ "	\"message\": \"{type}<br/><br/><br/>Error Description:&nbsp;&nbsp;&nbsp;{msg}<br/><br/>{stackTrace}<br/><br/>Caused by:<br/>{cause}\"";
				String portugueseErrorTemplate = errorTemplateWithMessage
				+ "}";
				String englishLanguage = JnLanguage.english.name();
				String englishTokenTemplate = "{"
				+ "	\"language\": \"" + englishLanguage + "\","
				+ "	\"templateId\": \"" + tokenTemplateId + "\","
				+ "	\"subject\": \"Token to access Onias.Site\","
				+ "	\"message\": \"<html><head><style>p,h4{ font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;}#customers {  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;  border-collapse: collapse;  width: 100%;}#customers td, #customers th {  border: 1px solid #ddd;  padding: 8px;}#customers tr:nth-child(even){background-color: #f2f2f2;}#customers tr:hover {background-color: #ddd;}#customers th {  padding-top: 12px;  padding-bottom: 12px;  text-align: left;  background-color: #7FBCEC;  color: white;}</style></head><body><div><p>Greetings, in case you do not know me or do not remember me, I am <a href='{linkedinAddress}' target = '_blank'>{linkedinName}</a></p><p>I am sending you a token to access <a href='{accessLink}' target = '_blank'>Onias.Site</a>; this token is {token} so that you can set up your user</p></div>" + JnEmailFooter.english.getValue() + "</body></html>\""
				+ "}";
				String englishErrorTemplate = "{"
				+ "	\"language\": \"" + englishLanguage + "\","
				+ "	\"templateId\": \"" + errorTemplateId + "\","
				+ "	\"subject\": \"[ERROR] {type}\","
				+ "	\"message\": \"{type}<br/><br/><br/>Error Description:&nbsp;&nbsp;&nbsp;{msg}<br/><br/>{stackTrace}<br/><br/>Caused by:<br/>{cause}\""
				+ "}";
				List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY,
				portugueseTokenTemplate,
				portugueseErrorTemplate,
				englishTokenTemplate,
				englishErrorTemplate

				);

		return createBulkItems;
	}
}
