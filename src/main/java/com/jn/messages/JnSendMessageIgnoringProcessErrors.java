package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.business.CcpBusiness;
import com.jn.business.messages.JnBusinessNotifyError;
import com.jn.business.messages.JnBusinessNotifySupport;
import com.jn.entities.JnEntityJobsnowWarning;

/**
 * Variante do builder que captura exceções nos steps, registra o warning e tenta notificar o
 * suporte via {@code JnBusinessNotifySupport}. Usada por {@code JnBusinessNotifyError} para evitar
 * loops infinitos de erros ao notificar falhas de envio.
 */
public class JnSendMessageIgnoringProcessErrors extends JnSendMessageToUser{

	
	public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		CcpBusiness lenientProcess = values -> {
			try {
				CcpJsonRepresentation apply = step.apply(values);
				return apply;
			} catch (Exception e) {
				CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
				String name = JnBusinessNotifyError.class.getName();
				JnSendMessageToUser x = new JnSendMessageAndJustErrors();
				JnBusinessNotifySupport.INSTANCE.apply(errorDetails, name, JnEntityJobsnowWarning.ENTITY, x);
				return values;
			}
		};
		JnSendMessageToUser addFlow = super.addOneStep(lenientProcess, parameterEntity, messageEntity, blockEntity, alreadySentEntity);
		return addFlow;
	}	
}
