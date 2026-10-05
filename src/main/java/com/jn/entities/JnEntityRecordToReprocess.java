package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Bulk operations that failed and must be reprocessed: the original JSON, the entity, the id, the error type and the reprocessing status. Written only by the reprocessing mapper.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_record_to_reprocess}</li>
 * <li>read-only: save, delete and transfer do nothing</li>
 * </ul>
 */
@CcpEntityOlyReadable
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityRecordToReprocess.Fields.class)
public class JnEntityRecordToReprocess implements CcpEntityConfigurator {
	
	/** The entity {@code jn_record_to_reprocess}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityRecordToReprocess.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code timestamp} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp, 
		/** The {@code operation} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		operation,
		/** The {@code entity} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity, 
		/** The {@code id} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id,
		/** The {@code json} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		json, 
		/** The {@code status} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		status,
		/** The {@code reason} field: text. */
		@CcpJsonFieldTypeString
		reason, 
		/** The {@code errorType} field: text. */
		@CcpJsonFieldTypeString
		errorType
		;
	}
}
