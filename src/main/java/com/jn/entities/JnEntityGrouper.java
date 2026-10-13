package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Generic group of records: a parent (an id, the kind of group and the values the id came from) and every child of
 * an entity that belongs to it, with all of its data. The children are split in pages ({@code childPage}), each page a
 * record: a search reads the pages of the group by id (parent id, parent name, child name and page) and gets the
 * children ready, with no query, one page at a time. The resumes, for instance, are grouped by each of their hashes
 * ({@code parentName = resumesGrouping}, {@code childName = vis_resume}). The writers change a page with an atomic
 * script ({@code CcpBulkEntityOperationType.scriptedUpdate}), so concurrent writers of the same page do not overwrite
 * each other.
 */
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
		/**
		 * The {@code childPage} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. A group is
		 * split in pages, each one a record holding a share of the children; a child always falls in the same page, chosen
		 * by its identity, so adding or removing it touches a single record.
		 */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		childPage,
		/** The {@code childJson} field: required, list, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		childJson,
		/** The {@code childCount} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		childCount,
		;
	}
}
