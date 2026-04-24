package com.jn.entities;

import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType.save;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityOperationStepType.after;
import static com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityType.mainEntity;

import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityAsyncWriter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperation;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOperations;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.login.JnBusinessNotifyAboutPendingUnlockLoginToken;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.decorators.JnAsyncWriterEntity;
import com.jn.entities.decorators.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

@CcpEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityTwin(
		twinEntityName = "login_token_fulfilled_unlock",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)

@CcpEntityCache(3600)
@CcpEntityDisposable(expurgTime = CcpEntityExpurgableOptions.daily, expurgableEntityFactory = JnDisposableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityLoginTokenRequestUnlock.Fields.class)
@CcpEntityOperations(
		operations = {
				@CcpEntityOperation(when = after, operation = save, into = mainEntity,  execute = {JnBusinessNotifyAboutPendingUnlockLoginToken.class}, operationHandlers = {}),
		},
		globalHandlers = {}
		)

public class JnEntityLoginTokenRequestUnlock implements CcpEntityConfigurator {
	
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityLoginTokenRequestUnlock.class).entityInstance;
	 
	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		supportAgent
		;
	}
}
