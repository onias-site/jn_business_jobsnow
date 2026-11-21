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
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Fixed parameters of the platform by name, lists of values that do not depend on the language (the language-dependent
 * texts are in {@link JnEntitySystemMessage}). Seeded with the list of non-professional e-mail domains; the vis seeds the
 * links of the Telegram groups.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_system_parameter}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntitySystemParameter.Fields.class)
public class JnEntitySystemParameter implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntitySystemParameter INSTANCE = new JnEntitySystemParameter();

	private JnEntitySystemParameter() {}

	/** The entity {@code jn_system_parameter}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySystemParameter.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code systemMessageName} field: the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		systemMessageName,
		/** The {@code message} field: required, list, text (the values of the parameter). */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray
		@CcpJsonFieldTypeString
		message,
		;

	}

	/** Name of the system parameter that lists the non-professional e-mail domains. */
	public static final String NON_PROFESSIONAL_DOMAINS = "NONPROFESSIONALDOMAINS";

	/**
	 * The primary key of a parameter.
	 * @param systemMessageName the name of the parameter
	 * @return the JSON with {@code systemMessageName}
	 */
	public static CcpJsonRepresentation getKey(String systemMessageName) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(Fields.systemMessageName, systemMessageName);
		return key;
	}

	/**
	 * The values of a parameter, read by id (cached for an hour by the entity).
	 * @param systemMessageName the name of the parameter
	 * @return the lines of {@code message}
	 */
	public static List<String> getValues(String systemMessageName) {
		CcpJsonRepresentation key = getKey(systemMessageName);
		CcpJsonRepresentation parameter = ENTITY.getOneById(key);
		List<String> values = parameter.getAsStringList(JnJsonCommonsFields.message);
		return values;
	}

	/**
	 * Seeds the list of non-professional e-mail domains (public webmail providers).
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

		CcpJsonRepresentation key = getKey(NON_PROFESSIONAL_DOMAINS);

		CcpJsonRepresentation nonProfessionalDomainsParameter = key
				.put(JnJsonCommonsFields.message, nonProfessionalDomains);

		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(
				ENTITY
				, nonProfessionalDomainsParameter
				);

		return createBulkItems;
	}

}
