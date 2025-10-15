package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityJobsnowWarning;

public class JnSendMessageAndJustErrors extends JnSendMessageToUser{

	
	public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity) {
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
		JnSendMessageToUser addFlow = super.addOneStep(process, parameterEntity, messageEntity);
		return addFlow;
	}	
}
