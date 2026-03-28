package com.jn.entities;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.annotations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityOlyReadable
@CcpEntitySpecifications(
		entityFieldsTransformers = JnJsonTransformersFieldsEntityDefault.class,
		entityValidation = JnEntityDisposableRecord.Fields.class,
		afterDeleteRecord = {},
		beforeSaveRecord = {},
		afterSaveRecord = {},
		flow = {}
)
public class JnEntityDisposableRecord implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityDisposableRecord.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		timestamp,
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 4, maxLength = 100)
		format,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		date,
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		json,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		id, 
		;
	}
	
	 
	
	 public static CcpJsonRepresentation getDataWithTimeStamp(CcpEntity entity, CcpJsonRepresentation json) {
		
		CcpJsonRepresentation transformedJsonByEachFieldInJson = entity.getTransformedJsonByEachFieldInJson(json);
		String id = entity.getPrimaryKeyValues(transformedJsonByEachFieldInJson).asUgglyJson();
		String entityName = entity.getEntityName();
		
		CcpJsonRepresentation idToSearch = CcpOtherConstants
				.EMPTY_JSON
				.put(Fields.id, id)
				.put(Fields.entity, entityName)
				;
		
		
		CcpJsonRepresentation oneById = ENTITY.getOneById(idToSearch);
		CcpJsonRepresentation jsonPiece = oneById.getJsonPiece(Fields.json, Fields.timestamp, Fields.format, Fields.date);
		Long timestamp = jsonPiece.getAsLongNumber(Fields.timestamp);
		String format = jsonPiece.getAsString(Fields.format);
		
		String dateItWasSaved = CcpEntityExpurgableOptions.getPastDate(format, timestamp);
		
		CcpJsonRepresentation innerJson = json.getInnerJson(Fields.json);
		CcpJsonRepresentation removeFields = jsonPiece.removeFields(Fields.json, Fields.timestamp, Fields.format);
		CcpJsonRepresentation renameField = removeFields.renameField(Fields.date, ExtraFields.expirationDate);
		CcpJsonRepresentation mergeWithAnotherJson = innerJson.mergeWithAnotherJson(renameField);
		CcpJsonRepresentation put = mergeWithAnotherJson.put(ExtraFields.dateItWasSaved, dateItWasSaved);
		
		return put;
	
	}
}

enum ExtraFields implements CcpJsonFieldName{
	 dateItWasSaved,
	 expirationDate
}

