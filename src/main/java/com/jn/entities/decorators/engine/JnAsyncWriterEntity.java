package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.jn.mensageria.JnFunctionMensageriaSender;

/**
 * Decorador que transforma operações síncronas de entidade em operações assíncronas via mensageria.
 * Qualquer chamada de {@code save}, {@code delete}, {@code deleteAnyWhere}, {@code transferDataTo}
 * ou {@code copyDataTo} é interceptada e enviada ao PubSub via {@code JnFunctionMensageriaSender}.
 */
public class JnAsyncWriterEntity extends CcpEntityDelegator  {

	public JnAsyncWriterEntity(CcpEntity entity) {
		super(entity);
	}

	public boolean delete(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.delete);
		return sent;
	}

	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.deleteAnyWhere);
		return sent;
	}

	public boolean save(CcpJsonRepresentation json) {
		boolean sent = this.sendToMensageria(json, CcpEntityOperationType.save);
		return sent;
	}

	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity entities) {
		CcpJsonRepresentation put = json.put(CcpEntityOperationType.Fields.entityToTransfer, entities);
		boolean sent = this.sendToMensageria(put, CcpEntityOperationType.transferDataTo);
		return sent;
	}

	public boolean copyDataTo(CcpJsonRepresentation json, CcpEntity entities) {
		CcpJsonRepresentation put = json.put(CcpEntityOperationType.Fields.entityToTransfer, entities);
		boolean sent = this.sendToMensageria(put, CcpEntityOperationType.copyDataTo);
		return sent;
	}

	/**
	 * Envia a operação para a mensageria. Como a execução é assíncrona, no momento da chamada ainda não
	 * há como saber se o documento será incluído, atualizado ou removido, então o retorno indica apenas
	 * que a mensagem foi aceita pelo tópico. Este é o único ponto da hierarquia onde o booleano não
	 * carrega o mesmo significado definido em {@code CcpEntity}.
	 */
	private boolean sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation) {
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, operation);
		CcpJsonRepresentation apply = sender.execute(json);
		boolean emptyResponse = apply.isEmpty();
		boolean sent = false == emptyResponse;
		return sent;
	}
}
