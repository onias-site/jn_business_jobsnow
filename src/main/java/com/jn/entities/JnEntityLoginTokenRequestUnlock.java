package com.jn.entities;

import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterDeleteFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;
import static com.jn.entities.decorators.enums.JnEntitySendMessageToUserWhenWriteOperationType.afterSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError;

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
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
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
import com.jn.entities.fields.transformers.JnJsonTransformersFieldEntityPasswordRandom;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDoNothing;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnDeleteKeysFromCache;
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
			operationType = afterSaveFromMainEntitySendAnInstantMessageAndIfFailsThrowAnError,
			messageTemplate = JnMessages.JnNotifySupportAboutPendingLockedLoginToken.class
			),
})
@CcpEntityTwin(
		twinEntityName = "login_token_fulfilled_unlock",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)

@CcpEntityCache(3600)
@JnEntityAsyncWriter(JnAsyncWriterEntity.class)
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityLoginTokenRequestUnlock.Fields.class)


/**
 * Registra a solicitação de desbloqueio de token de login feita pelo usuário. Comportamento
 * idêntico a {@code JnEntityLoginTokenRequestResend}, mas notifica o suporte via
 * {@code NotifySupportAboutPendingUnlockLoginToken}. Escrita assíncrona, descartável diariamente.
 */
public class JnEntityLoginTokenRequestUnlock implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityLoginTokenRequestUnlock.class).entityInstance;
	 
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
		email,
		
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		chatId, 
		
		@CcpJsonFieldTypeString(exactLength = 8)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldEntityPasswordRandom.class)
		password
		;
	}
}
