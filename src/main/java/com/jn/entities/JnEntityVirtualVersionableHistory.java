package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Virtual entity whose id identifies, in {@code JnEntityGrouper}, the history of a record of a versionable entity
 * ({@code @CcpEntityVersionable}): the name of the entity of the record ({@code entity}) and the id of the record
 * ({@code id}, its document id, calculated from its primary key). The group has the name of the entity of the record as
 * {@code childName}, so the histories of one entity are found by the parent name and the child name, and one child per
 * write of the record: a JSON text with the {@code operation}, the {@code date}, the {@code timestamp} and the record as it
 * was before the write ({@code json}), in the order of the writes. Its {@code parentJson} also keeps the values of the
 * primary key of the record ({@code primaryKey}).
 * <p>
 * Until 2026-11-20 each write was a row of its own in {@code jn_versionable}, found only by a query; the history of a
 * record is now read by id. Cached for 1 hour.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityVirtualVersionableHistory.Fields.class)
public class JnEntityVirtualVersionableHistory {

	/** The entity {@code jn_virtual_versionable_history}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityVirtualVersionableHistory.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code entity} field: part of the primary key, the name of the versionable entity of the record. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity,

		/** The {@code id} field: part of the primary key, the document id of the record. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id,
		;
	}
}
