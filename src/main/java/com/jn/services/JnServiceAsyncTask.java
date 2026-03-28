package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

public enum JnServiceAsyncTask implements JnService {
	GetAsyncTaskStatusById{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			//FIXME

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
