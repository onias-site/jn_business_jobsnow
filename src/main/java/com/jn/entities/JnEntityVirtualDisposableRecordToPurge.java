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
 * Virtual entity whose id identifies, in {@code JnEntityGrouper}, the group of a record of a disposable entity whose
 * expired records are purged ({@code @CcpEntityDisposable(removeCompletely = true)}, the default): the same {@code entity}
 * and {@code id} (the primary key as compact JSON) of its copy in {@link JnEntityDisposableRecord}. Every write of the
 * record puts in the group the document of the period written, as a JSON text with the {@code entity} (the index), the
 * {@code id} of the document and the {@code timestamp} of the end of its period; the daily purge
 * ({@code JnBusinessPurgeExpiredDisposableRecords}) reads these groups to find what has expired. The records of an entity
 * with {@code removeCompletely = false} are in no group, so they are never purged. Cached for 1 hour.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityVirtualDisposableRecordToPurge.Fields.class)
public class JnEntityVirtualDisposableRecordToPurge {

	/** The entity {@code jn_virtual_disposable_record_to_purge}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityVirtualDisposableRecordToPurge.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code entity} field: part of the primary key, the name of the disposable entity of the record. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity,

		/** The {@code id} field: part of the primary key, the primary key of the record as compact JSON. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id,
		;
	}
}
