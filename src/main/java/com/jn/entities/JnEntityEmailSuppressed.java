package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterInsertFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.email.suppression.JnBusinessPublishEmailSuppressed;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The addresses the e-mail provider keeps suppressing (permanent bounce, invalid address, spam report): one record per
 * address, whatever the subject, because the provider suppresses the address and not a kind of message. The sending of
 * messages checks it to stop sending to them. An address found in two lists keeps the last one written. Written by
 * {@code JnBusinessLoadEmailSuppressions}. Until 2026-11-01 only {@code jn_email_reported_as_spam} existed, keyed by subject
 * type and written by nobody, so no sending was ever blocked. The first write of an address is announced to whoever listens
 * to it ({@link JnBusinessPublishEmailSuppressed}).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_email_suppressed}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityEmailSuppressed.Fields.class)
@CcpAfterInsertFromMainEntity(execute = {JnBusinessPublishEmailSuppressed.class}, operationHandlers = {})
public class JnEntityEmailSuppressed implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityEmailSuppressed INSTANCE = new JnEntityEmailSuppressed();

	private JnEntityEmailSuppressed() {}

	/** The entity {@code jn_email_suppressed}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailSuppressed.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,
		/** The {@code suppressionList} field: required, the list of the provider the address came from. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = CcpEmailSuppressionList.class)
		suppressionList,
		/** The {@code created} field: required, when the address entered the list, as a Unix timestamp in seconds. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		created,
		/** The {@code reason} field: text, the reason given by the provider; a spam report has none. */
		@CcpJsonFieldTypeString
		reason,
		/** The {@code status} field: the status code of the failure; only the bounces have it. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		status,
		/** The {@code ip} field: the address given with a spam report. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		ip
		;
	}

}
