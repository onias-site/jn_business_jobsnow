package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

public enum JnServiceContactUs implements JnService {
	SaveContactUs{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	ListContactUsByUser{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	GetContactUsKpis{
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	;
	enum JsonFieldNames implements CcpJsonFieldName{
		activePosition
	}
	enum SaveContactUs{
		
	}
	enum ListContactUsByUser{
		
	}
	enum GetContactUsKpis{
		
	}
}
