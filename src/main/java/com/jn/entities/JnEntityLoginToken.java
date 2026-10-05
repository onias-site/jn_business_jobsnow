package com.jn.entities;

import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperation;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.business.login.JnBusinessPrepareLoginTokenBeforeSave;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
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
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * The token e-mailed to the user to define the password (onboarding or recovery); the twin holds the tokens locked after too many wrong attempts.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_login_token}</li>
 * <li>records cached for 86400 seconds</li>
 * <li>on {@code beforeSaveFromMainEntity} runs {@code JnBusinessPrepareLoginTokenBeforeSave}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError} sends {@code JnNotifyUserAboutLoginToken}</li>
 * <li>written asynchronously, through messaging</li>
 * <li>twin entity {@code jn_login_token_locked}</li>
 * <li>disposable: records expire by the {@code monthly} granularity</li>
 * </ul>
 */
@CcpEntityCache(86400)
@CcpEntityCustomDecorators(value = {
		@CcpEntityCustomDecorator(value = JnEntityAsyncWriterBuilder.class, priority = 8),
		@CcpEntityCustomDecorator(value = JnEntityDisposableBuilder.class, priority = 1)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserBeforeWriteBuilder.class, priority = 7)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserAfterWriteBuilder.class, priority = 5)
		})
@CcpEntityOperations({
		@CcpEntityOperation(operationType = CcpEntityOperationType.beforeSaveFromMainEntity,  execute = {JnBusinessPrepareLoginTokenBeforeSave.class}, operationHandlers = {}),
})
@JnEntitySendMessageToUserWhenWrite({
		@JnEntitySendMessageToUserWhenWriteOperation(
				operationType = afterInsertFromMainEntitySendAnEmailMessageAndIfFailsThrowAnError,
				messageTemplate = JnMessages.JnNotifyUserAboutLoginToken.class
				)
})
@JnEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityTwin(
		twinEntityName = "jn_login_token_locked",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.monthly)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityLoginToken.Fields.class)
public class JnEntityLoginToken implements CcpEntityConfigurator {
	
	/** The entity {@code jn_login_token}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityLoginToken.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,  
		/** The {@code token} field: validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		token
		;
	}
}
