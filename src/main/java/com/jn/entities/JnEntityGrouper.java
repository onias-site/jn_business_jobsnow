package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDoNotDeleteKeysFromCache;

/**
 * Generic group of records: a parent (an id, the kind of group and the values the id came from) and the id of every
 * child of an entity that belongs to it ({@code child}), in a single record: a search reads the group by id (parent id,
 * parent name and child name) and then the children by their ids, with no query. The resumes, for instance, are grouped by
 * each of their hashes ({@code parentName = vis_virtual_hash_grouper}, the entity that calculates the hashes;
 * {@code childName = vis_resume}). The writers change a
 * group with an atomic script ({@code CcpBulkEntityOperationType.scriptedUpdate}), so concurrent writers of the same
 * group do not overwrite each other.
 * <p>
 * Twin entity ({@code jn_inactive_grouper}): the children on the main side of their entity are in the main groups, the
 * ones on the twin side (the inactive resumes, for instance) in the twin groups, under the same group keys. The entities
 * annotated with {@code @JnEntityGrouperChild} keep both sides up to date.
 */
@CcpEntityTwin(
		twinEntityName = "jn_inactive_grouper",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		// the grouper is not cached: its writes delete no cache key (until 2026-10-14 each group written deleted one)
		functionToDeleteKeysInTheCacheClass = JnDoNotDeleteKeysFromCache.class
		)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityGrouper.Fields.class)
public class JnEntityGrouper implements CcpEntityConfigurator {

	/** The entity {@code jn_grouper}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityGrouper.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code parentId} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		parentId,
		/** The {@code parentName} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		parentName,
		/** The {@code parentJson} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		parentJson,
		/** The {@code childName} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		childName,
		/** The {@code child} field: required, list of ids, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		child,
		/** The {@code childCount} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		childCount,
		;
	}
}
