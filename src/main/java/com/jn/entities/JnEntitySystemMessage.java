package com.jn.entities;

import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;

/**
 * Fixed messages of the platform by name and language (internationalization). Seeded with the list of non-professional e-mail domains.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_system_message}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySystemMessage.Fields.class)
public class JnEntitySystemMessage implements CcpEntityConfigurator {

	/** The entity {@code jn_system_message}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySystemMessage.class).entityInstance;
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code systemMessageName} field: part of the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		systemMessageName, 
		/** The {@code language} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language,
		/** The {@code message} field: required, list, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray
		@CcpJsonFieldTypeString
		message,
		;

	}

	/** Name of the system message that lists the non-professional e-mail domains. */
	public static final String NON_PROFESSIONAL_DOMAINS = "NONPROFESSIONALDOMAINS";

	/**
	 * Seeds, in Portuguese, the list of non-professional e-mail domains (public webmail providers).
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {

		List<String> nonProfessionalDomains = Arrays.asList(
				  "globalweb.com.br"
				, "localweb.com.br"
				, "locaweb.com.br"
				, "protonmail.com"
				, "outlook.com.br"
				, "outlook.com"
				, "hotmail.com.br"
				, "hotmail.com"
				, "live.com.br"
				, "live.com"
				, "msn.com"
				, "yahoo.com.br"
				, "yahoo.com"
				, "ymail.com"
				, "rocketmail.com"
				, "terra.com.br"
				, "uol.com.br"
				, "uolinc.com"
				, "bol.com.br"
				, "ig.com.br"
				, "superig.com.br"
				, "gmail.com"
				, "googlemail.com"
				, "icloud.com"
				, "me.com"
				, "mac.com"
				, "aol.com"
				, "zipmail.com.br"
				, "oi.com.br"
				, "r7.com"
				, "globo.com"
				, "globomail.com"
				, "click21.com.br"
				, "pop.com.br"
				, "brturbo.com.br"
				, "itelefonica.com.br"
				, "gmx.com"
				, "mail.com"
				, "inbox.com"
				, "yandex.com"
				, "zoho.com"
				, "tutanota.com"
				, "fastmail.com"
				, "hushmail.com"
				, "mail.ru"
				);

		CcpJsonRepresentation withName = CcpOtherConstants.EMPTY_JSON
				.put(Fields.systemMessageName, NON_PROFESSIONAL_DOMAINS);

		CcpJsonRepresentation comIdioma = withName
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese);

		CcpJsonRepresentation nonProfessionalDomainsMessage = comIdioma
				.put(JnJsonCommonsFields.message, nonProfessionalDomains);

		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(
				ENTITY
				, nonProfessionalDomainsMessage
				);

		return createBulkItems;
	}

}
