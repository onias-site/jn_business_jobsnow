package com.jn.entities;

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
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.decorators.annotations.JnEntityDisposable;
import com.jn.entities.decorators.builders.JnEntityDisposableBuilder;
import com.jn.entities.decorators.engine.JnDisposableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Contacts of the "Contact Us" form ignored by support; the twin keeps the ones read again.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_contact_us_ignored}</li>
 * <li>records cached for 86400 seconds</li>
 * <li>disposable: records expire by the {@code daily} granularity</li>
 * <li>twin entity {@code jn_contact_us_reread}</li>
 * </ul>
 */
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityDisposableBuilder.class, priority = 1),})
@CcpEntityCache(86400)
@JnEntityDisposable(value = JnDisposableEntity.class, timeOption = CcpEntityExpurgableOptions.daily)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityContactUsIgnored.Fields.class)
@CcpEntityTwin(
		twinEntityName = "jn_contact_us_reread",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JnEntityContactUsIgnored implements CcpEntityConfigurator {
	
	/** The entity {@code jn_contact_us_ignored}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityContactUsIgnored.class).entityInstance;
	
	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code email} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		email
		;

	}

}
