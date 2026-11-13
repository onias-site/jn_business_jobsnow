package com.jn.entities;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnRecordsCounterEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * How many records an entity annotated with {@code @CcpEntityRecordsCounter} has now on its main side and on its twin
 * side: one record per entity, rewritten by {@link JnRecordsCounterEntity} after each insertion, deactivation,
 * reactivation and delete of its records (for {@code vis_resume}: the active and the inactive resumes).
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityRecordsCount.Fields.class)
public class JnEntityRecordsCount implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityRecordsCount INSTANCE = new JnEntityRecordsCount();

	private JnEntityRecordsCount() {}

	/** The entity {@code jn_records_count}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityRecordsCount.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code entityName} field: the primary key, the name of the main side of the counted entity. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 100)
		entityName,
		/** The {@code twinEntityName} field: the name of the twin side, the same of {@code entityName} when it has none. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 100)
		twinEntityName,
		/** The {@code mainRecords} field: how many records the main side has. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		mainRecords,
		/** The {@code twinRecords} field: how many records the twin side has (0 when it has none). */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		twinRecords,
		;
	}
}
