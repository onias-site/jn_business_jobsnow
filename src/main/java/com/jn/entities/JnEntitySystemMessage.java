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

@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySystemMessage.Fields.class)
/**
 * Armazena mensagens do sistema exibidas na interface, indexadas por {@code systemMessageName} e
 * {@code language}. Permite internacionalização de mensagens fixas da plataforma. Cache de 1 hora.
 */
public class JnEntitySystemMessage implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySystemMessage.class).entityInstance;
	
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		systemMessageName, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language,
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray
		@CcpJsonFieldTypeString
		message,
		;

	}

	/** Nome da mensagem de sistema que guarda os domínios de e-mail não corporativos. */
	public static final String NON_PROFESSIONAL_DOMAINS = "NONPROFESSIONALDOMAINS";

	/**
	 * Carga inicial da entidade: o registro com a lista de domínios de e-mail não corporativos
	 * (provedores gratuitos e de webmail), que serve para distinguir um e-mail pessoal de um e-mail
	 * de empresa. A lista tem como semente os domínios que estavam fixos em
	 * {@code CcpEmailDecorator} e que passam a morar no banco.
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

		CcpJsonRepresentation comNome = CcpOtherConstants.EMPTY_JSON
				.put(Fields.systemMessageName, NON_PROFESSIONAL_DOMAINS);

		CcpJsonRepresentation comIdioma = comNome
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
