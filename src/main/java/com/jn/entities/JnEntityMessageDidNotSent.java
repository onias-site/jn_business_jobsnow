package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnMustNotSendMessage;

/**
 * Messages that were not sent because a rule forbade it (see {@code JnMustNotSendMessage}), with the reason and its details.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_message_did_not_sent}</li>
 * <li>records cached for 3600 seconds</li>
 * </ul>
 */
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityMessageDidNotSent.Fields.class)
@CcpEntityCache(3600)

public class JnEntityMessageDidNotSent implements CcpEntityConfigurator {
	
	/** The entity {@code jn_message_did_not_sent}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityMessageDidNotSent.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		
		/** The {@code subjectType} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		
		/**
		 * Nome da entidade cuja pesquisa no union-all impediu o envio, tal como gravado por
		 * {@code JnMustNotSendMessage} e pesquisado por {@code JnServiceLogin}.
		 */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		reasonType,

		/** The item of {@link JnMustNotSendMessage} (the list of entities) the reason belongs to; required. */
		@CcpJsonFieldTypeString(allowedValuesEnum = JnMustNotSendMessage.class)
		@CcpJsonFieldValidatorRequired
		reasonDescription,
		
		
		/** The {@code reasonDetails} field: part of the primary key, required, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = JnReasonDetails.class)
		reasonDetails,
		
		/** The {@code reasonMessage} field: text. */
		@CcpJsonFieldTypeString
		reasonMessage,
	}
	
	/** Details of the reason. */
	public static enum JnReasonDetails{
		/** A record that had to exist was not found. */
		isNotPresentInThisUnionAll,
		/** The message lacked fields of the primary key of a checked entity. */
		missingFieldsToPrimaryKey,
		/** A record that forbids the sending was found. */
		isPresentInThisUnionAll,
	}
	

}
