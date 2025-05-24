package com.jn.mensageria;

import java.util.function.Consumer;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnMensageriaReceiver extends CcpMensageriaReceiver{
	
	private JnMensageriaReceiver() {
		super(JnEntityAsyncTask.Fields.operationType.name(), JnEntityAsyncTask.Fields.operation.name());
	}
	
	public static final JnMensageriaReceiver INSTANCE = new JnMensageriaReceiver();
	
	private JnMensageriaReceiver saveResult(
			CcpEntity entity, 
			CcpJsonRepresentation messageDetails, 
			Throwable e,
			Function<CcpJsonRepresentation, CcpJsonRepresentation> jnAsyncBusinessNotifyError
			) {
		CcpJsonRepresentation response = new CcpJsonRepresentation(e);
		JnMensageriaReceiver saveResult = this.saveResult(entity, messageDetails, response, false);
		return saveResult;
		
	}

	private JnMensageriaReceiver saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response) {
		JnMensageriaReceiver saveResult = this.saveResult(entity, messageDetails, response, true);
		return saveResult;
	}
	
	
	
	public JnMensageriaReceiver executeProcess(
			CcpEntity entity,
			String processName, 
			CcpJsonRepresentation json,
			Function<CcpJsonRepresentation, CcpJsonRepresentation> jnAsyncBusinessNotifyError
			) {
		try {
			Function<CcpJsonRepresentation, CcpJsonRepresentation> process = this.getProcess(processName, json);
			CcpJsonRepresentation response = process.apply(json);
			JnMensageriaReceiver saveResult = this.saveResult(entity, json, response);
			return saveResult;
		} catch (Throwable e) {
			JnMensageriaReceiver saveResult = this.saveResult(entity, json, e, jnAsyncBusinessNotifyError);
			return saveResult;
		}
	}
	
	private JnMensageriaReceiver saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response, boolean success) {
		Long finished = System.currentTimeMillis();
		CcpJsonRepresentation oneById = entity.getOneById(messageDetails);
		Long started = oneById.getAsLongNumber(JnEntityAsyncTask.Fields.started.name());
		Long enlapsedTime = finished - started;
		CcpJsonRepresentation processResult = messageDetails
				.put(JnEntityAsyncTask.Fields.enlapsedTime.name(), enlapsedTime)
				.put(JnEntityAsyncTask.Fields.response.name(), response)
				.put(JnEntityAsyncTask.Fields.finished.name(), finished)
				.put(JnEntityAsyncTask.Fields.success.name(), success);
		entity.createOrUpdate(processResult);
		return this;
	}

	public CcpExecuteBulkOperation getExecuteBulkOperation() {
		return JnExecuteBulkOperation.INSTANCE;
	}

	public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
		return JnDeleteKeysFromCache.INSTANCE;
	}

}
