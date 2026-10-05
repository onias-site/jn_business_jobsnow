package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;

/** Services about asynchronous tasks. */
public enum JnServiceAsyncTask implements JnService {
	/** Returns the status of an asynchronous task (not implemented yet: it returns the request). */
	GetAsyncTaskStatusById{
		/**
		 * Not implemented yet.
		 * @param json the request
		 * @return the same request
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			//LATER

//			String asyncTaskId = json.getAsString(JsonFieldNames.asyncTaskId);

			//			CcpJsonRepresentation execute = JnEntityAsyncTask.ENTITY.getOneById(asyncTaskId);
			return  json;
		}
	},
	;
	/** Fields of the request. */
	public static enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code asyncTaskId} field. */
		asyncTaskId
	}
}

/** Input rules of the {@code GetAsyncTaskStatusById} service (none yet). */
enum GetAsyncTaskStatusById implements CcpJsonFieldName{

}
