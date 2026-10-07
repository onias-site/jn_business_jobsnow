package com.jn.services;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityAsyncTask;

/** Services about asynchronous tasks. */
public enum JnServiceAsyncTask implements JnService {
	/**
	 * Returns the status of an asynchronous task, read by id from {@code jn_async_task}. Until 2026-10-07 it was a stub
	 * that answered the request itself with 200, so the client could not tell a missing implementation from a task
	 * without status.
	 */
	GetAsyncTaskStatusById{
		/**
		 * Reads the task and keeps only its status fields: {@code request} and {@code response} carry the data of the
		 * task (a password, for instance) and are never exposed. A task still running has no {@code finished},
		 * {@code success} or {@code enlapsedTime} yet.
		 * @param json the request, with {@code asyncTaskId}
		 * @return the status of the task
		 * @throws com.ccp.flow.CcpErrorFlowDisturb with status 404 when there is no such task
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String asyncTaskId = json.getAsString(JsonFieldNames.asyncTaskId);
			CcpJsonRepresentation taskKey = CcpOtherConstants.EMPTY_JSON.put(JnEntityAsyncTask.Fields.messageId, asyncTaskId);
			CcpJsonRepresentation task = JnEntityAsyncTask.ENTITY.getOneById(taskKey);
			CcpJsonRepresentation taskStatus = task.getJsonPiece(
					JnEntityAsyncTask.Fields.messageId,
					JnEntityAsyncTask.Fields.topic,
					JnEntityAsyncTask.Fields.operation,
					JnEntityAsyncTask.Fields.started,
					JnEntityAsyncTask.Fields.finished,
					JnEntityAsyncTask.Fields.enlapsedTime,
					JnEntityAsyncTask.Fields.success
					);
			return taskStatus;
		}
	},
	;
	/** Fields of the request. */
	public static enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code asyncTaskId} field. */
		asyncTaskId
	}
}

/** Input rules of the {@code GetAsyncTaskStatusById} service. */
enum GetAsyncTaskStatusById implements CcpJsonFieldName{
	/** The id of the task ({@code messageId} of {@code jn_async_task}). */
	@CcpJsonFieldValidatorRequired
	asyncTaskId
}
