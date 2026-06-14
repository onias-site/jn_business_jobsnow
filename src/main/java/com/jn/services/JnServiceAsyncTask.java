package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

/**
 * Serviço de operações sobre tarefas assíncronas. Contém o valor {@code GetAsyncTaskStatusById}
 * (implementação pendente — retorna o JSON de entrada sem alteração).
 */
public enum JnServiceAsyncTask implements JnService {
	GetAsyncTaskStatusById{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			//LATER

//			String asyncTaskId = json.getAsString(JsonFieldNames.asyncTaskId);

			//			CcpJsonRepresentation execute = JnEntityAsyncTask.ENTITY.getOneById(asyncTaskId);
			return  json;
		}
	},
	;
	public static enum JsonFieldNames implements CcpJsonFieldName{
		asyncTaskId
	}
	enum GetAsyncTaskStatusById{
		
	}
}
