package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.jn.entities.JnEntityAsyncTask;

public enum JnServiceAsyncTask implements JnService {
	GetAsyncTaskStatusById{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			String asyncTaskId = json.getAsString(JsonFieldNames.asyncTaskId);
			CcpJsonRepresentation execute = JnEntityAsyncTask.ENTITY.getOneById(asyncTaskId);
			return  execute;
		}
	},
	;
	public static enum JsonFieldNames implements CcpJsonFieldName{
		asyncTaskId
	}
	enum GetAsyncTaskStatusById{
		
	}
}
