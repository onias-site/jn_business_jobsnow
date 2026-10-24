package com.jn.entities;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.annotations.operations.CcpAfterSaveFromMainEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNestedJson;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.business.bots.command.allowed.JnSupportAllowCommandToUserBotConfiguration;
import com.jn.business.bots.ignored.users.JnSupportIgnoredUsersBotConfiguration;
import com.jn.business.ignored.JnBusinessPublishIgnoredUserSaved;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnUserRequestCommands;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Users that the support bot operator chose to ignore, because they only play with the requests
 * (absurd requests, swear words): one record per user ({@code email}, kept as hash), with the command in which the
 * operator decided it ({@code commandName}) and the request that led to the decision ({@code description}). The
 * ignoring is global: the requests of a user found here no longer reach the operator in any command, of any cost center
 * (skill suggestions and skill hierarchy fixes of vis, tickets of the "contact us" of js). Queried on every request of
 * the user, hence the 1-hour cache. Until 2026-10-23 it belonged to vis ({@code vis_command_not_allowed_to_user}); it
 * moved to jn when js started reading it.
 * Has the twin entity jn_command_reallowed_to_user, only for control and tracking: deleting a record (the
 * support bot {@code allowCommandToUser} command) moves it there, so what was undone stays recorded.
 * Saving a user announces it to the cost centers that listen to it ({@link JnBusinessPublishIgnoredUserSaved}), which
 * discard every request of the user still pending, as if it had never existed.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityCommandNotAllowedToUser.Fields.class)
@CcpAfterSaveFromMainEntity(execute = {JnBusinessPublishIgnoredUserSaved.class}, operationHandlers = {})
@CcpEntityTwin(
		twinEntityName = "jn_command_reallowed_to_user",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JnEntityCommandNotAllowedToUser implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityCommandNotAllowedToUser INSTANCE = new JnEntityCommandNotAllowedToUser();

	private JnEntityCommandNotAllowedToUser() {}

	/** The entity {@code jn_command_not_allowed_to_user}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityCommandNotAllowedToUser.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,

		/** The {@code commandName} field: the command in which the operator ignored the user (only informative), text, required. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = JnUserRequestCommands.class)
		commandName,

		/**
		 * The {@code description} field: nested JSON, required. The request that led to the decision, with the readable
		 * {@code email} of the user and the summary shown to the operator in the lists of ignored users
		 * ({@code JnJsonCommonsFields.request}: the skill of a skill suggestion, the parent of a skill hierarchy fix, the title
		 * of a ticket of the "contact us").
		 */
		@CcpJsonFieldTypeNestedJson(allowsEmptyJson = false)
		@CcpJsonFieldValidatorRequired
		description,

	}

	/**
	 * Seeds the commands of the support bot that undo the ignoring: {@code /allowCommandToUser <email>} and
	 * {@code /ignoredUsers}. Since 2026-10-23 jn seeds them, and not vis.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> allowCommandToUserItems = JnSupportAllowCommandToUserBotConfiguration.getSeeds();
		List<CcpBulkItem> ignoredUsersItems = JnSupportIgnoredUsersBotConfiguration.getSeeds();
		List<CcpBulkItem> firstRecords = new ArrayList<>(allowCommandToUserItems);
		firstRecords.addAll(ignoredUsersItems);
		return firstRecords;
	}
}
