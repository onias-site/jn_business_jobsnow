package com.jn.entities.decorators.builders;

import java.lang.reflect.Constructor;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpCustomDecoratorEntity;
import com.jn.entities.decorators.annotations.JnEntitySendMessageToUserWhenTransfer;

/**
 * Constrói o decorator que envia as mensagens do fluxo {@code after} das transferências de dados.
 * Declare-o em {@code @CcpEntityCustomDecorators} com prioridade baixa, para que fique na parte
 * interna da cadeia e só dispare depois da transferência.
 */
public class JnEntitySendMessageToUserAfterTransferBuilder extends CcpCustomDecoratorEntity{

	@SuppressWarnings("unchecked")
	public CcpEntity getEntity(Class<?> configurationClass, CcpEntity entity) {
		var annotation = configurationClass.getAnnotation(JnEntitySendMessageToUserWhenTransfer.class);
		Class<CcpEntity> value = (Class<CcpEntity>) annotation.afterDecoratorClass();

		try {
			Constructor<CcpEntity> constructor = value.getConstructor(CcpEntity.class, JnEntitySendMessageToUserWhenTransfer.class);
			CcpEntity newInstance = constructor.newInstance(entity, annotation);
			return newInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
