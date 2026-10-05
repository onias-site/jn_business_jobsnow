package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumber;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * An asynchronous task triggered through messaging: start and end times, elapsed time, data, topic, original request, Pub/Sub message id, whether it succeeded, the operation run and its response.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_async_task}</li>
 * </ul>
 */
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityAsyncTask.Fields.class)
public class JnEntityAsyncTask implements CcpEntityConfigurator {

	/** The entity {@code jn_async_task}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityAsyncTask.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code started} field: required, decimal number. */
		@CcpJsonFieldValidatorRequired  
		@CcpJsonFieldTypeNumber
		started, 
		/** The {@code finished} field: decimal number. */
		@CcpJsonFieldTypeNumber
		finished, 
		/** The {@code enlapsedTime} field: non-negative integer. */
		@CcpJsonFieldTypeNumberUnsigned
		enlapsedTime, 
		/** The {@code data} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		data,
		/** The {@code topic} field: required, text. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString
		topic, 
		/** The {@code request} field: required. */
		@CcpJsonFieldValidatorRequired
		request, 
		/** The {@code messageId} field: text, part of the primary key. */
		@CcpJsonFieldTypeString
		@CcpEntityFieldPrimaryKey
		messageId, 
		/** The {@code success} field: boolean. */
		@CcpJsonFieldTypeBoolean
		success,
		/** The {@code operation} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		operation,
		/** The {@code response} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		response
		;
		
	}
}
