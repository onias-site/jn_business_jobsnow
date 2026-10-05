package com.jn.entities;

import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.annotations.JnEntityAsyncWriter;
import com.jn.entities.decorators.annotations.JnEntityDisposable;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWrite;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenWriteOperation;
import com.jn.entities.decorators.builders.JnEntityAsyncWriterBuilder;
import com.jn.entities.decorators.builders.JnEntityDisposableBuilder;
import com.jn.entities.decorators.builders.JnEntitySendMessageToUserAfterWriteBuilder;
import com.jn.entities.decorators.builders.JnEntitySendMessageToUserBeforeWriteBuilder;
import com.jn.entities.decorators.engine.JnAsyncWriterEntity;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Errors of the system, keyed by stack trace hash and type so the same error is not recorded twice while the record lives; each new error notifies support.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_jobsnow_error}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning} sends {@code JnNotifySupportAboutAnError}</li>
 * <li>written asynchronously, through messaging</li>
 * <li>disposable: records expire by the {@code hourly} granularity</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {
		@CcpEntityCustomDecorator(value = JnEntityDisposableBuilder.class, priority = 1)
		,@CcpEntityCustomDecorator(value = JnEntityAsyncWriterBuilder.class, priority = 8)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserBeforeWriteBuilder.class, priority = 7)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserAfterWriteBuilder.class, priority = 5)
		})


@JnEntitySendMessageToUserWhenWrite({
		@JnEntitySendMessageToUserWhenWriteOperation(
				operationType = afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning,
				messageTemplate = JnMessages.JnNotifySupportAboutAnError.class
				)
})
@JnEntityAsyncWriter(JnAsyncWriterEntity.class)
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.hourly)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityJobsnowError.Fields.class)
public class JnEntityJobsnowError implements CcpEntityConfigurator {

	/** The entity {@code jn_jobsnow_error}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityJobsnowError.class).entityInstance;
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code cause} field: list, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorArray(nonRepeatedItems = false)
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		cause, 
		/** The {@code stackTrace} field: list, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorArray(nonRepeatedItems = false)
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		stackTrace, 
		/** The {@code stackTraceHash} field: part of the primary key, text. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		stackTraceHash,
		/** The {@code type} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		type, 
		/** The {@code message} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		message
		;
	}
}
