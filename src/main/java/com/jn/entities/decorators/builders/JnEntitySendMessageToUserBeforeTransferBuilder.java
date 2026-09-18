package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Constrói o decorator que envia as mensagens do fluxo {@code before} das transferências de dados.
 * Declare-o em {@code @CcpEntityCustomDecorators} com prioridade alta, para que fique na parte
 * externa da cadeia.
 */
public class JnEntitySendMessageToUserBeforeTransferBuilder extends CcpCustomDecoratorEntity{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntitySendMessageToUserWhenTransfer.class);
		Class<CcpEntity> value = (Class<CcpEntity>) annotation.beforeDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = value.getConstructor(CcpEntity.class, JnEntitySendMessageToUserWhenTransfer.class);
			CcpEntity newInstance = constructor.newInstance(entity, annotation);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
