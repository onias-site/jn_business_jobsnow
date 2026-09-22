package com.jn.messages;

import java.lang.reflect.Field;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.entities.JnEntityMessageDidNotSent.JnReasonDetails;

public enum JnMustNotSendMessage{

	alreadySentEntities(true),
	parameterEntities(false),
	messageEntities(false),
	blockEntities(true)
	;
	final boolean whenPresentInUnionAll;
	
	
	private JnMustNotSendMessage(boolean whenPresentInThisUnionAll) {
		this.whenPresentInUnionAll = whenPresentInThisUnionAll;
	}
	
	@SuppressWarnings("unchecked")
	protected List<CcpEntity> getEntities(JnSendMessageToUser obj){
		try {
			String name = this.name();
			Field declaredField = JnSendMessageToUser.class.getDeclaredField(name);
			declaredField.setAccessible(true);
			var get = declaredField.get(obj);
			List<CcpEntity> listCcpEntity = (List<CcpEntity>)get;
			return listCcpEntity;
			
		} catch (Exception e) {
			JnErrorMessageEntitiesNotAccessible jnErrorMessageEntitiesNotAccessible = new JnErrorMessageEntitiesNotAccessible(this, e);
			throw jnErrorMessageEntitiesNotAccessible;
		}
	}

	/**
	 * Exceção lançada quando a lista de entidades correspondente a este item não pode ser lida por reflexão
	 * em {@code JnSendMessageToUser}, o que indica que o campo foi renomeado ou removido.
	 */
	@SuppressWarnings("serial")
	public static class JnErrorMessageEntitiesNotAccessible extends RuntimeException {
		/**
		 * Monta a mensagem informando qual campo não pôde ser lido e encadeia a exceção original como causa.
		 * @param field o item cujo campo homônimo era esperado em {@code JnSendMessageToUser}
		 * @param cause a exceção original de reflexão
		 */
		private JnErrorMessageEntitiesNotAccessible(JnMustNotSendMessage field, Throwable cause) {
			super("The field '" + field + "' could not be read from JnSendMessageToUser", cause);
		}
	}
	
	private void saveMessageNotSent(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, 
			CcpJsonRepresentation json, Integer index, 
			JnReasonDetails reasonDetails, String reasonMessage) {
		
		List<CcpEntity> entities = this.getEntities(obj);
		CcpEntity entity = entities.get(index);
		CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
		String reasonDescription = this.name();
		CcpJsonRepresentation put = json
				.put(JnEntityMessageDidNotSent.Fields.reasonDetails,  reasonDetails)
				.put(JnEntityMessageDidNotSent.Fields.reasonType,  entityMetaData.entityName)
				.put(JnEntityMessageDidNotSent.Fields.reasonDescription,  reasonDescription)
				;
		CcpJsonRepresentation jsonToSave = this.putReasonMessage(put, reasonMessage);
		JnEntityMessageDidNotSent.ENTITY.save(jsonToSave);
		throw new MessageDidNotSend(jsonToSave);
	}

	/**
	 * O campo é opcional na entidade, que por outro lado não aceita string vazia. Quando o motivo não
	 * traz mensagem — caso do registro simplesmente ausente no union-all — o campo é retirado do json,
	 * para que a gravação do diagnóstico não seja recusada pela validação.
	 */
	private CcpJsonRepresentation putReasonMessage(CcpJsonRepresentation json, String reasonMessage) {

		boolean thereIsNoReasonMessage = reasonMessage.trim().isEmpty();

		if(thereIsNoReasonMessage) {
			CcpJsonRepresentation removeFields = json.removeFields(JnEntityMessageDidNotSent.Fields.reasonMessage);
			return removeFields;
		}

		CcpJsonRepresentation put = json.put(JnEntityMessageDidNotSent.Fields.reasonMessage, reasonMessage);
		return put;
	}
	
	@SuppressWarnings("serial")
	private static class MessageDidNotSend extends RuntimeException{
		protected MessageDidNotSend(CcpJsonRepresentation json) {
			super(json.toString());
		}
	}
	
	public void validate(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, CcpJsonRepresentation json, Integer index) {
		
		JnReasonDetails reasonDetails = JnReasonDetails.isNotPresentInThisUnionAll;
		
		String reasonMessage = "";
		
		List<CcpEntity> entities = this.getEntities(obj);
		
		CcpEntity entity = entities.get(index);
		
		try {
			boolean isPresentInThisUnionAll  = entity.isPresentInThisUnionAll(unionAll, json);

			if(isPresentInThisUnionAll) {
				reasonDetails = JnReasonDetails.isPresentInThisUnionAll;
			}
			
			boolean mustSendMessage = this.whenPresentInUnionAll != isPresentInThisUnionAll;
			
			if(mustSendMessage) {
				return;
			}
			
		} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
			reasonDetails = JnReasonDetails.missingFieldsToPrimaryKey;
			reasonMessage = e.getMessage();
		}
	
		this.saveMessageNotSent(obj, unionAll, json, index, reasonDetails, reasonMessage);
	
	}

}
