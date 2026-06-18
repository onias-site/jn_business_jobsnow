package com.jn.entities;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityOlyReadable;
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

/**
 * Registro de expiração (disposable) de outras entidades. Armazena uma cópia do JSON de uma entidade
 * com timestamp de expiração. Usado por {@code JnDisposableEntity} para implementar TTL sem depender
 * de recurso nativo do Elasticsearch. Somente leitura — nunca gravado diretamente.
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
		;
	}
	
	public static CcpJsonRepresentation getDataWithTimeStamp(CcpJsonRepresentation oneById) {
		CcpJsonRepresentation jsonPiece = oneById.getJsonPiece(Fields.json, Fields.timestamp, Fields.format);
		Long timestamp = jsonPiece.getAsLongNumber(Fields.timestamp);
		String format = jsonPiece.getAsString(Fields.format);
		String newFormat = "dd/MM/yyyy à HH:mm";
		String dateItWasSaved = CcpEntityExpurgableOptions.getPastDate(format, newFormat, timestamp).replace("à", "às");
		CcpTimeDecorator ctd = new CcpTimeDecorator(timestamp);
		String expirationDate = ctd.getFormattedDateTime(newFormat).replace("à", "às");
		
		CcpJsonRepresentation innerJson = oneById.getInnerJson(Fields.json);
		CcpJsonRepresentation removeFields = jsonPiece.removeFields(Fields.json, Fields.format);
		CcpJsonRepresentation renameField = removeFields.put(ExtraFields.expirationDate, expirationDate);
		CcpJsonRepresentation mergeWithAnotherJson = innerJson.mergeWithAnotherJson(renameField);
		CcpJsonRepresentation put = mergeWithAnotherJson.put(ExtraFields.dateItWasSaved, dateItWasSaved);
		return put;
	}
}

enum ExtraFields implements CcpJsonFieldName{
	 dateItWasSaved,
	 expirationDate
}

