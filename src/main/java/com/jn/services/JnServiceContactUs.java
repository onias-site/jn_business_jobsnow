package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;


/** "Contact us" services; none implemented yet, all return the request. */
public enum JnServiceContactUs implements JnService {
	/** Saves a contact (not implemented yet). */
	SaveContactUs{
		/**
		 * Not implemented yet.
		 * @param json the request
		 * @return the same request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	/** Lists the contacts of a user (not implemented yet). */
	ListContactUsByUser{
		/**
		 * Not implemented yet.
		 * @param json the request
		 * @return the same request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	/** Returns the indicators of the contacts (not implemented yet). */
	GetContactUsKpis{
		/**
		 * Not implemented yet.
		 * @param json the request
		 * @return the same request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return  json;
		}
	},
	;
}

/** Input rules of the {@code SaveContactUs} service (none yet). */
enum SaveContactUs implements CcpJsonFieldName{

}

/** Input rules of the {@code ListContactUsByUser} service (none yet). */
enum ListContactUsByUser implements CcpJsonFieldName{

}

/** Input rules of the {@code GetContactUsKpis} service (none yet). */
enum GetContactUsKpis implements CcpJsonFieldName{

}
