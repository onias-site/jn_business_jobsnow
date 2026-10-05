package com.jn.mensageria;

import java.util.function.Consumer;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.decorators.builders.JnEntityAsyncWriterBuilder;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Receives the messages of messaging and runs, for each one, the business or the entity operation named by the topic
 * (and the {@code operation} field); the outcome (success or error, response, end time and elapsed time) is recorded
 * back in {@code jn_async_task}.
 */
public class JnMensageriaReceiver extends CcpMensageriaReceiver{
	
	/** Singleton; use {@link #INSTANCE}. The operation is read from the {@code operation} field. */
	private JnMensageriaReceiver() {
		super(JnJsonCommonsFields.operation.name());
	}
	
	/** The single instance. */
	public static final JnMensageriaReceiver INSTANCE = new JnMensageriaReceiver();
	
	/**
	 * Records a failed outcome.
	 * @param entity the entity of the asynchronous tasks
	 * @param messageDetails the message
	 * @param e the failure
	 * @return this instance
	 */
	private JnMensageriaReceiver saveResult(
			CcpEntity entity, 
			CcpJsonRepresentation messageDetails, 
			Throwable e
			) {
		CcpJsonRepresentation response = new CcpJsonRepresentation(e);
		JnMensageriaReceiver saveResult = this.saveResult(entity, messageDetails, response, false);
		return saveResult;
		
	}

	/**
	 * Records a successful outcome.
	 * @param entity the entity of the asynchronous tasks
	 * @param messageDetails the message
	 * @param response the response of the task
	 * @return this instance
	 */
	private JnMensageriaReceiver saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response) {
		JnMensageriaReceiver saveResult = this.saveResult(entity, messageDetails, response, true);
		return saveResult;
	}
	
	
	
	/**
	 * Runs the task and records its outcome; a failure is recorded, not rethrown.
	 * @param entity the entity of the asynchronous tasks
	 * @param processName the topic
	 * @param json the message
	 * @return this instance
	 */
	public JnMensageriaReceiver executeProcess(
			CcpEntity entity,
			String processName, 
			CcpJsonRepresentation json
			) {
		try {
			CcpBusiness process = this.getProcess(processName, json);
			CcpJsonRepresentation response = process.execute(json);
			JnMensageriaReceiver saveResult = this.saveResult(entity, json, response);
			return saveResult;
		} catch (Throwable e) {
			JnMensageriaReceiver saveResult = this.saveResult(entity, json, e);
			return saveResult;
		}
	}
	
	/**
	 * Records the outcome of the processing in the asynchronous task.
	 * <p>The start comes from the message itself, not from a database query: {@code JnFunctionMensageriaSender} writes
	 * {@code started} in the JSON <b>before</b> publishing it, so the field arrives with the message. Reading the record
	 * just to get that number cost one database call per message consumed, in every asynchronous flow of the system.</p>
	 * @param entity the entity of the asynchronous tasks
	 * @param messageDetails the message
	 * @param response the response or the error
	 * @param success whether the task succeeded
	 * @return this instance
	 */
	private JnMensageriaReceiver saveResult(CcpEntity entity, CcpJsonRepresentation messageDetails, CcpJsonRepresentation response, boolean success) {
		Long finished = System.currentTimeMillis();
		Long started = messageDetails.getOrDefault(JnEntityAsyncTask.Fields.started, () -> finished);
		Long enlapsedTime = finished - started;
		CcpJsonRepresentation put = messageDetails
				.put(JnEntityAsyncTask.Fields.enlapsedTime, enlapsedTime);
				CcpJsonRepresentation put2 = put
				.put(JnJsonCommonsFields.response, response);
				CcpJsonRepresentation put3 = put2
				.put(JnEntityAsyncTask.Fields.finished, finished);
				CcpJsonRepresentation processResult = put3
				.put(JnEntityAsyncTask.Fields.success, success);
		entity.save(processResult);
		return this;
	}

	/**
	 * The bulk executor of the jn cost center.
	 * @return {@link JnExecuteBulkOperation#INSTANCE}
	 */
	public CcpExecuteBulkOperation getExecuteBulkOperation() {
		return JnExecuteBulkOperation.INSTANCE;
	}

	/**
	 * The cache cleanup of the jn cost center.
	 * @return {@link JnDeleteKeysFromCache#INSTANCE}
	 */
	public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
		return JnDeleteKeysFromCache.INSTANCE;
	}
	
	/**
	 * Builds the entity of the configurator without the asynchronous writer, so the consumer writes for real.
	 * @param newInstance the configurator
	 * @return the entity
	 */
	protected CcpEntity getCustomEntity(Object newInstance) {
		CcpEntityConfigurator configurator = (CcpEntityConfigurator)newInstance;
		CcpEntity entity = CcpEntityFactory.getCustomEntity(configurator, JnEntityAsyncWriterBuilder.INSTANCE);
		return entity;
	}

	/**
	 * Returns the twin of the entity without the asynchronous writer.
	 * @param entity the entity
	 * @return the twin
	 */
	protected CcpEntity getTwinEntity(CcpEntity entity) {
		CcpEntity twinEntity = entity.getTwinEntity(JnEntityAsyncWriterBuilder.INSTANCE);
		return twinEntity;
	}

}
