package com.jn.messages;

import java.lang.reflect.Field;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityPrimaryKeyIsMissing;

enum MustNotSendMessage{

	alreadySentEntities(true),
	parameterEntities(false),
	messageEntities(false),
	blockEntities(true)
	;
	final boolean whenPresentInUnionAll;
	
	
	private MustNotSendMessage(boolean whenPresentInThisUnionAll) {
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
		private JnErrorMessageEntitiesNotAccessible(MustNotSendMessage field, Throwable cause) {
			super("The field '" + field + "' could not be read from JnSendMessageToUser", cause);
		}
	}
	
	private boolean mustNotSendMessage(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, CcpJsonRepresentation json, Integer index) {
		
		List<CcpEntity> entities = this.getEntities(obj);
		
		CcpEntity entity = entities.get(index);
		
		try {
			boolean isPresentInThisUnionAll  = entity.isPresentInThisUnionAll(unionAll, json);
			boolean mustNotSendMessage = this.whenPresentInUnionAll == isPresentInThisUnionAll;
			return mustNotSendMessage;
		} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
			return false;
		}
	}

	public static void validate(JnSendMessageToUser obj, CcpSelectUnionAll unionAll, CcpJsonRepresentation json, Integer index) {
		MustNotSendMessage[] values = MustNotSendMessage.values();
		for (MustNotSendMessage item : values) {
			boolean mustNotSendMessage2 = item.mustNotSendMessage(obj, unionAll, json, index);
			boolean mustSendMessage = false == mustNotSendMessage2;
			if(mustSendMessage) {
				continue;
			}
			JnSendMessageToUser.CcpMessageDidNotSend ccpMessageDidNotSend = new JnSendMessageToUser.CcpMessageDidNotSend(obj, item, json, index);
			throw ccpMessageDidNotSend;
		}
	}
}
