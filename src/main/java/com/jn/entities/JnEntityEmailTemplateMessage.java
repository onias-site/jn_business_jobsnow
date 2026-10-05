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
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.builders.JnEntityVersionableBuilder;
import com.jn.entities.decorators.builders.JnEntityVersionablePurgeBuilder;
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
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityEmailTemplateMessage.Fields.class)
public class JnEntityEmailTemplateMessage  implements CcpEntityConfigurator{

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
				+ "	\"subject\": \"Envio de token para acesso ao sistema JobsNow\",";
				String tokenTemplateWithMessage = tokenTemplateWithSubject
				+ "	\"message\": \"<html><head><style>p,h4{ font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;}#customers {  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;  border-collapse: collapse;  width: 100%;}#customers td, #customers th {  border: 1px solid #ddd;  padding: 8px;}#customers tr:nth-child(even){background-color: #f2f2f2;}#customers tr:hover {background-color: #ddd;}#customers th {  padding-top: 12px;  padding-bottom: 12px;  text-align: left;  background-color: #7FBCEC;  color: white;}</style></head><body><div><p>Saudações, caso você não me conheça ou não se lembre de mim, sou o <a href='{linkedinAddress}' target = '_blank'>{linkedinName}</a></p><p>Estou estou enviando um token para acesso ao sistema <a href='{accessLink}' target = '_blank'>JobsNow</a> esse token é {token} para você poder configurar seu usuário</p><p>Você pode receber vagas (condizentes com o teu perfil profissional cadastrado no site do JobsNow) ou currículos (de acordo com as vagas que você cadastrar) como notificação no seu celular <b>INSTANTÂNEAMENTE</b> se seguir os seguintes passos: </p><p>A) Instalar o telegram no seu celular </p><p>B) Procurar  na mensagem fixada <a href='{telegramGroupLink}' target = '_blank'>deste grupo de vagas</a> do telegram <a href = '{botAddress}' target = '_blank'> a este bot aqui</a> </p><p>C) Informar o e-mail  {email} para este bot</p><p>D) Depois de informar seu e-mail a este bot, informe o token {token} </p></div></body></html>\"";
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
				+ "	\"subject\": \"Token to access the JobsNow system\","
				+ "	\"message\": \"<html><head><style>p,h4{ font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;}#customers {  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;  border-collapse: collapse;  width: 100%;}#customers td, #customers th {  border: 1px solid #ddd;  padding: 8px;}#customers tr:nth-child(even){background-color: #f2f2f2;}#customers tr:hover {background-color: #ddd;}#customers th {  padding-top: 12px;  padding-bottom: 12px;  text-align: left;  background-color: #7FBCEC;  color: white;}</style></head><body><div><p>Greetings, in case you do not know me or do not remember me, I am <a href='{linkedinAddress}' target = '_blank'>{linkedinName}</a></p><p>I am sending you a token to access the <a href='{accessLink}' target = '_blank'>JobsNow</a> system; this token is {token} so that you can set up your user</p><p>You can receive job openings (matching the professional profile you registered on the JobsNow website) or resumes (according to the job openings you register) as notifications on your phone <b>INSTANTLY</b> if you follow these steps: </p><p>A) Install Telegram on your phone </p><p>B) In the pinned message of <a href='{telegramGroupLink}' target = '_blank'>this Telegram job openings group</a>, look for <a href = '{botAddress}' target = '_blank'>this bot here</a> </p><p>C) Give the email  {email} to this bot</p><p>D) After giving your email to this bot, give it the token {token} </p></div></body></html>\""
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
