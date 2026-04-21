package com.jn.entities;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDetails;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldNotUpdatable;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

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
		;
	}
	
	 
	
	 public static CcpJsonRepresentation getDataWithTimeStamp(CcpEntity entity, CcpJsonRepresentation json) {
		
		CcpJsonRepresentation idToSearch = getIdToSearch(entity, json);
		
		CcpJsonRepresentation oneById = ENTITY.getEntityDetails().getOneByIdOrHandleItIfThisIdWasNotFound(idToSearch, CcpOtherConstants.RETURNS_EMPTY_JSON);
		
		CcpJsonRepresentation whenFieldsAreFound = oneById.whenFieldsAreFound(x -> getDataWithTimeStamp(x), Fields.format);
		
		return whenFieldsAreFound;
	
	}

	public static CcpJsonRepresentation getDataWithTimeStamp(CcpJsonRepresentation oneById) {
		CcpJsonRepresentation jsonPiece = oneById.getJsonPiece(Fields.json, Fields.timestamp, Fields.format, Fields.date);
		Long timestamp = jsonPiece.getAsLongNumber(Fields.timestamp);
		String format = jsonPiece.getAsString(Fields.format);
		
		String dateItWasSaved = CcpEntityExpurgableOptions.getPastDate(format, timestamp);
		
		CcpJsonRepresentation innerJson = oneById.getInnerJson(Fields.json);
		CcpJsonRepresentation removeFields = jsonPiece.removeFields(Fields.json, Fields.timestamp, Fields.format);
		CcpJsonRepresentation renameField = removeFields.renameField(Fields.date, ExtraFields.expirationDate);
		CcpJsonRepresentation mergeWithAnotherJson = innerJson.mergeWithAnotherJson(renameField);
		CcpJsonRepresentation put = mergeWithAnotherJson.put(ExtraFields.dateItWasSaved, dateItWasSaved);
		return put;
	}



	public static CcpJsonRepresentation getIdToSearch(CcpEntity entity, CcpJsonRepresentation json) {
		CcpEntityDetails entityDetails = entity.getEntityDetails();
		CcpJsonRepresentation handledJson = entity.getHandledJson(json);
		String id = entityDetails.getPrimaryKeyValues(handledJson).asUgglyJson();
		String entityName = entityDetails.entityName;
		
		CcpJsonRepresentation idToSearch = CcpOtherConstants
				.EMPTY_JSON
				.put(Fields.id, id)
				.put(Fields.entity, entityName)
				;
		return idToSearch;
	}
}

enum ExtraFields implements CcpJsonFieldName{
	 dateItWasSaved,
	 expirationDate
}

