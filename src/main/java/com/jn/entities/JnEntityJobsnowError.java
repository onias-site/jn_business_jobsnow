package com.jn.entities;

import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityAsyncWriter;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsLogTheError;
import com.ccp.especifications.db.utils.entity.decorators.annotations.messages.write.CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.bots.stack.trace.JnSupportStackTraceBotConfiguration;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.engine.JnAsyncWriterEntity;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnApiOrigins;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Errors of the system, keyed by stack trace hash and type so the same error is not recorded twice while the record lives; each new error notifies support and opens a ticket for the operator ({@code /solveStackTrace <errorId>}).
* <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_jobsnow_error}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning} sends {@code JnNotifySupportAboutAnError}</li>
 * <li>on {@code afterInsertFromMainEntitySendAnInstantMessageAndIfFailsLogTheError} sends {@code JnNotifySupportAboutStackTraceTicket}: an annotation of its own because the messages of one annotation are chained, and the notice would inherit the chat and the text of the ticket; the ticket is recorded before its delivery, so a failure only needs the log</li>
 * <li>written asynchronously, through messaging</li>
 * <li>disposable: records expire by the {@code hourly} granularity, and the documents of the past periods stay, because a ticket may wait longer than that</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityAsyncWriter(JnAsyncWriterEntity.class)
@CcpEntityFieldsValidator(JnEntityJobsnowError.Fields.class)
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.hourly, removeCompletely = false)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsSaveAWarning(JnMessages.JnNotifySupportAboutAnError.class)
@CcpAfterInsertFromMainEntitySendAnInstantMessageAndIfFailsLogTheError(JnMessages.JnNotifySupportAboutStackTraceTicket.class)
public class JnEntityJobsnowError implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityJobsnowError INSTANCE = new JnEntityJobsnowError();

	private JnEntityJobsnowError() {}

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
		message,
		/** The {@code origin} field: text, the API where the error happened ({@code JnApiOrigins}). */
		@CcpJsonFieldTypeString(allowedValuesEnum = JnApiOrigins.class)
				origin
		;
	}

	/**
	 * Seeds the command of the support bot that closes the ticket of an error: {@code /solveStackTrace <errorId>}.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> firstRecords = JnSupportStackTraceBotConfiguration.getSeeds();
		return firstRecords;
	}
}
