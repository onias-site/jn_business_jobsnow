package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldNotUpdatable;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;


/**
 * Expiration copy of the records of the disposable entities: the record JSON plus the expiration timestamp, written by {@code JnDisposableEntity} to implement a time to live without a native Elasticsearch feature. Never saved directly.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_disposable_record}</li>
 * <li>read-only: save, delete and transfer do nothing</li>
 * </ul>
 */
@CcpEntityOlyReadable
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityDisposableRecord.Fields.class)
public class JnEntityDisposableRecord implements CcpEntityConfigurator {

	/** The entity {@code jn_disposable_record}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityDisposableRecord.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
	
		/** The {@code timestamp} field: not updatable, required, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp,
		/** The {@code format} field: not updatable, required, text. */
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 4, maxLength = 100)
		format,
		/** The {@code entity} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity, 
		/** The {@code date} field: not updatable, required, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		date,
		/** The {@code json} field: required, validated as in {@code JnJsonCommonsFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		json,
		/** The {@code id} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id, 
		/** The {@code trueTimestamp} field: not updatable, required, non-negative integer. */
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		trueTimestamp,
		;
	}
	
	/**
	 * Returns the original record kept in the expiration copy plus {@code dateItWasSaved} and {@code expirationDate}
	 * (both as {@code dd/MM/yyyy - HH:mm}) and the timestamps of the copy.
	 * @param oneById the expiration copy
	 * @return the original record with the dates
	 */
	public static CcpJsonRepresentation getDataWithTimeStamp(CcpJsonRepresentation oneById) {
		CcpJsonRepresentation jsonPiece = oneById.getJsonPiece(JnJsonCommonsFields.json, JnJsonCommonsFields.timestamp, Fields.trueTimestamp);
		Long trueTimestamp = jsonPiece.getAsLongNumber(Fields.trueTimestamp);
		Long timestamp = jsonPiece.getAsLongNumber(JnJsonCommonsFields.timestamp);
		String newFormat = "dd/MM/yyyy - HH:mm";
		CcpTimeDecorator savingTime = new CcpTimeDecorator(trueTimestamp);
		String dateItWasSaved = savingTime.getFormattedDateTime(newFormat);
		CcpTimeDecorator expirationTime = new CcpTimeDecorator(timestamp);
		String expirationDate = expirationTime.getFormattedDateTime(newFormat);

		CcpJsonRepresentation innerJson = oneById.getInnerJson(JnJsonCommonsFields.json);
		CcpJsonRepresentation timestamps = jsonPiece.removeFields(JnJsonCommonsFields.json, Fields.format);
		CcpJsonRepresentation timestampsWithExpirationDate = timestamps.put(JnJsonCommonsFields.expirationDate, expirationDate);
		CcpJsonRepresentation dataWithTimestamps = innerJson.mergeWithAnotherJson(timestampsWithExpirationDate);
		CcpJsonRepresentation dataWithSavingDate = dataWithTimestamps.put(JnJsonCommonsFields.dateItWasSaved, dateItWasSaved);
		return dataWithSavingDate;
	}
}


