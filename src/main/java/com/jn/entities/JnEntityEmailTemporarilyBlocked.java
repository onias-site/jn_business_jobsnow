package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The addresses whose mail server temporarily refused a message, kept only for diagnosis. It never blocks a sending,
 * because the provider itself keeps sending to them: a recruiter here is still a valid recipient, unlike the ones in
 * {@code jn_email_suppressed}. Written by {@code JnBusinessLoadEmailSuppressions}.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_email_temporarily_blocked}</li>
 * <li>disposable: records expire by the {@code monthly} granularity</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JnEntityEmailTemporarilyBlocked.Fields.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.monthly)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityEmailTemporarilyBlocked implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityEmailTemporarilyBlocked INSTANCE = new JnEntityEmailTemporarilyBlocked();

	private JnEntityEmailTemporarilyBlocked() {}

	/** The entity {@code jn_email_temporarily_blocked}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailTemporarilyBlocked.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,
		/** The {@code created} field: required, when the address entered the list, as a Unix timestamp in seconds. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		created,
		/** The {@code reason} field: text, the reason given by the provider. */
		@CcpJsonFieldTypeString
		reason,
		/** The {@code status} field: the status code of the failure. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		status
		;
	}

}
