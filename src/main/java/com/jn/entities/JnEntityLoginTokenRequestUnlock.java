package com.jn.entities;

import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;
import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
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
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDoNothing;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;
/**
 * A request of the user to unlock the login token: like {@code JnEntityLoginTokenRequestResend}, with the unlock notifications; the twin keeps the fulfilled requests.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_login_token_request_unlock}</li>
 * <li>on {@code afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutSolvedLockedLoginToken}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError} sends {@code JnNotifySupportAboutPendingLockedLoginToken}</li>
 * <li>twin entity {@code jn_login_token_fulfilled_unlock}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>written asynchronously, through messaging</li>
 * <li>disposable: records expire by the {@code daily} granularity</li>
 * </ul>
 */
@CcpEntityCustomDecorators(value = {
		@CcpEntityCustomDecorator(value = JnEntityDisposableBuilder.class, priority = 1)
		,@CcpEntityCustomDecorator(value = JnEntityAsyncWriterBuilder.class, priority = 8)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserBeforeWriteBuilder.class, priority = 7)
		,@CcpEntityCustomDecorator(value = JnEntitySendMessageToUserAfterWriteBuilder.class, priority = 5)
		})
@JnEntitySendMessageToUserWhenWrite({
	@JnEntitySendMessageToUserWhenWriteOperation(
			operationType = afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError,
			messageTemplate = JnMessages.JnNotifySupportAboutSolvedLockedLoginToken.class
			),
	@JnEntitySendMessageToUserWhenWriteOperation(
			operationType = afterInsertFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError,
			messageTemplate = JnMessages.JnNotifySupportAboutPendingLockedLoginToken.class
			),
})
@CcpEntityTwin(
		twinEntityName = "jn_login_token_fulfilled_unlock",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)

@CcpEntityCache(3600)
@JnEntityAsyncWriter(JnAsyncWriterEntity.class)
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityLoginTokenRequestUnlock.Fields.class)


public class JnEntityLoginTokenRequestUnlock implements CcpEntityConfigurator {
	
	/** The entity {@code jn_login_token_request_unlock}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityLoginTokenRequestUnlock.class).entityInstance;
	 
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}, transformed by {@code JnJsonTransformersFieldsEntityDoNothing}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
		email,
		
		/** The {@code chatId} field: validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId
		;
	}
}
