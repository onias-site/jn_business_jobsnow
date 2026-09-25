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
 * Receptor Singleton de mensagens do PubSub. Roteia cada mensagem recebida para o {@code CcpBusiness}
 * correto com base no campo {@code operation} ou no nome do tópico. Registra o resultado da execução
 * (sucesso ou erro) de volta em {@code JnEntityAsyncTask}, incluindo tempo decorrido e resposta.
 */
public class JnMensageriaReceiver extends CcpMensageriaReceiver{
	
	private JnMensageriaReceiver() {
		super(JnJsonCommonsFields.operation.name());
	}
	
	public static final JnMensageriaReceiver INSTANCE = new JnMensageriaReceiver();
	
	private JnMensageriaReceiver saveResult(
			CcpEntity entity, 
			CcpJsonRepresentation messageDetails, 
			Throwable e
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
	 * Registra o desfecho do processamento na tarefa assíncrona.
	 *
	 * <p>O instante de início vem da própria mensagem, e não de uma consulta ao banco:
	 * {@code JnFunctionMensageriaSender.getMessageDetails} grava {@code started} no json <b>antes</b>
	 * de publicá-lo, então o campo chega aqui junto com a mensagem. Buscar o registro só para reler
	 * esse número custava uma ida ao banco por mensagem consumida, em todo fluxo assíncrono do
	 * sistema.</p>
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

	public CcpExecuteBulkOperation getExecuteBulkOperation() {
		return JnExecuteBulkOperation.INSTANCE;
	}

	public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
		return JnDeleteKeysFromCache.INSTANCE;
	}
	
	protected CcpEntity getCustomEntity(Object newInstance) {
		CcpEntityConfigurator configurator = (CcpEntityConfigurator)newInstance;
		CcpEntity entity = CcpEntityFactory.getCustomEntity(configurator, JnEntityAsyncWriterBuilder.INSTANCE);
		return entity;
	}

	protected CcpEntity getTwinEntity(CcpEntity entity) {
		CcpEntity twinEntity = entity.getTwinEntity(JnEntityAsyncWriterBuilder.INSTANCE);
		return twinEntity;
	}

}
