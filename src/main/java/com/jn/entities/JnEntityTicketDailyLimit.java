package com.jn.entities;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityDisposable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnTicketQuotaGroups;

/**
 * The user ({@code email}, kept as hash) opened, in the last 24 hours, a ticket of the group of daily quota
 * {@code quotaGroup} ({@link JnTicketQuotaGroups}): one record per user and group, written when the ticket is saved and
 * valid for one day from then. While it lasts, a new ticket of the same group, of any cost center, is refused with the
 * {@code dailyLimitReached} (429) of the service that receives it. Each ticket emails the user and opens a ticket to the
 * support operator, so without this limit an automation could drain the email quota and load the backend.
 * <p>
 * Replaces, since 2026-10-23, {@code vis_skill_suggestion_daily_limit} and {@code vis_skill_fix_hierarchy_daily_limit},
 * one per kind of request: the skill suggestion, the skill hierarchy fix and the "contact us" that is neither a bug nor a
 * question now spend the same quota, a decision of the user.
 * <p>
 * The block lasts 24 hours from the ticket, not until midnight: after the turn of the day the document of the period no
 * longer matches, but its copy in {@code jn_disposable_record} expires one day after it was written, and
 * {@code JnDisposableEntity.exists} still accepts it until then.
 * <p>
 * Not cached: the check is a single read by id, and without cache the test scripts can free the day by deleting the
 * record straight from the database.
 */
@CcpEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityTicketDailyLimit.Fields.class)
public class JnEntityTicketDailyLimit implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityTicketDailyLimit INSTANCE = new JnEntityTicketDailyLimit();

	private JnEntityTicketDailyLimit() {}

	/** The entity {@code jn_ticket_daily_limit}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityTicketDailyLimit.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email,
		/** The {@code quotaGroup} field: part of the primary key, the group of daily quota of the ticket. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(allowedValuesEnum = JnTicketQuotaGroups.class)
		quotaGroup,
		;
	}

	/**
	 * The key of the quota of the user in the group, to be checked (by id) before a ticket and saved after it.
	 * @param email the e-mail of the user, as typed
	 * @param quotaGroup the group of daily quota of the ticket
	 * @return the key
	 */
	public static CcpJsonRepresentation getKey(String email, JnTicketQuotaGroups quotaGroup) {
		CcpJsonRepresentation keyWithEmail = CcpOtherConstants.EMPTY_JSON.put(Fields.email, email);
		CcpJsonRepresentation key = keyWithEmail.put(Fields.quotaGroup, quotaGroup.name());
		return key;
	}
}
