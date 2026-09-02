package com.jn.services;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.services.JnServiceLogin.JsonFieldNames;

class LoadDataAboutToken implements CcpBusiness{
	
	public static final LoadDataAboutToken INSTANCE = new LoadDataAboutToken();

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation innerJsonFromPath = json.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JnEntityDisposableRecord.ENTITY);
		CcpJsonRepresentation whenAnyFieldsAreFound = innerJsonFromPath.whenAnyFieldsAreFound(JsonTransformer.INSTANCE, JnJsonCommonsFields.timestamp);
		CcpJsonRepresentation jsonPiece = json.getJsonPiece(JnJsonCommonsFields.email, JsonFieldNames.sessionToken);
		CcpJsonRepresentation mergedJson = whenAnyFieldsAreFound.mergeWithAnotherJson(jsonPiece);
		return mergedJson;
		
	}
}
