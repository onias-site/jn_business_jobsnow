package com.jn.entities;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * The business that validates the session of the user, by class name: the cost center of the login seeds it, and the
 * APIs of the other cost centers run it without importing that cost center, the same way the bot runs its steps.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_session_validator}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityFieldsValidator(JnEntitySessionValidator.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntitySessionValidator implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntitySessionValidator INSTANCE = new JnEntitySessionValidator();

	private JnEntitySessionValidator() {}

	/** The entity {@code jn_session_validator}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySessionValidator.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code validatorName} field: the primary key, one of {@link JnSessionValidatorNames}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(allowedValuesEnum = JnSessionValidatorNames.class)
		validatorName,
		/** The {@code business} field: the name of the class of the business that validates the session. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 200)
		business,
		;
	}

	/**
	 * The key of the validator of the given name.
	 * @param validatorName the name of the validator
	 * @return the key
	 */
	public static CcpJsonRepresentation getKey(JnSessionValidatorNames validatorName) {
		String validatorNameValue = validatorName.name();
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(Fields.validatorName, validatorNameValue);
		return key;
	}

	/**
	 * The seed of the validator of the given name, for the {@code getFirstRecordsToInsert} of an entity of the cost
	 * center that owns the business.
	 * @param validatorName the name of the validator
	 * @param business the class of the business that validates the session
	 * @return the seed records
	 */
	public static List<CcpBulkItem> getSeeds(JnSessionValidatorNames validatorName, Class<? extends CcpBusiness> business) {
		CcpJsonRepresentation key = getKey(validatorName);
		String businessName = business.getName();
		CcpJsonRepresentation validator = key.put(Fields.business, businessName);
		List<CcpBulkItem> seeds = ENTITY.toBulkItems(validator, CcpBulkEntityOperationType.create);
		return seeds;
	}
}
