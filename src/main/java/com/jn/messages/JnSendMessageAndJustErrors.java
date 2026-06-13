package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityJobsnowWarning;

/**
 * Variante do builder que captura exceções nos steps e as registra em {@code JnEntityJobsnowWarning}
 * sem propagar a exceção. Usada quando o fluxo deve continuar mesmo que um canal de envio falhe,
 * registrando o problema como warning em vez de erro crítico.
 */
public class JnSendMessageAndJustErrors extends JnSendMessageToUser{

	
	public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		CcpBusiness process = values -> {
			try {
				CcpJsonRepresentation apply = step.apply(values);
				return apply;
			} catch (Exception e) {
				CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
				JnEntityJobsnowWarning.ENTITY.save(errorDetails);
				e.printStackTrace();
				return values;
			}
		};
		JnSendMessageToUser addFlow = super.addOneStep(process, parameterEntity, messageEntity, blockEntity, alreadySentEntity);
		return addFlow;
	}	
}
