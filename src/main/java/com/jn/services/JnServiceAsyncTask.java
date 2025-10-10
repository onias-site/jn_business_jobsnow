package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

public enum JnServiceAsyncTask implements JnService {
	GetAsyncTaskStatusById{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	;
	enum JsonFieldNames implements CcpJsonFieldName{
		activePosition
	}
	enum GetAsyncTaskStatusById{
		
	}
}
