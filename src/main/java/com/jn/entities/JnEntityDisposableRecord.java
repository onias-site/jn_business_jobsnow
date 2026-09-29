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
 * Expiration (disposable) record of other entities. Stores a copy of an entity's JSON with an
 * expiration timestamp. Used by {@code JnDisposableEntity} to implement TTL without relying on a
 * native Elasticsearch feature. Read-only — never saved directly.
 */
@CcpEntityOlyReadable
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityDisposableRecord.Fields.class)
public class JnEntityDisposableRecord implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityDisposableRecord.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
	
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp,
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 4, maxLength = 100)
		format,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity, 
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		date,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		json,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id, 
		@CcpEntityFieldNotUpdatable
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeNumberUnsigned
		trueTimestamp,
		;
	}
	
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


