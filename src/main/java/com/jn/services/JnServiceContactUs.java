package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;

/**
 * Serviço de gerenciamento de contatos ("Fale Conosco"). Os três valores ({@code SaveContactUs},
 * {@code ListContactUsByUser}, {@code GetContactUsKpis}) estão com implementações pendentes
 * (retornam o JSON de entrada sem alteração).
 */
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
