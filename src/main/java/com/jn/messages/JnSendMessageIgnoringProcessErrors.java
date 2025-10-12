package com.jn.messages;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.business.CcpBusiness;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.commons.JnBusinessNotifySupport;
import com.jn.entities.JnEntityJobsnowWarning;

public class JnSendMessageIgnoringProcessErrors extends JnSendMessageToUser{

	
	public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity) {
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
		JnSendMessageToUser addFlow = super.addOneStep(lenientProcess, parameterEntity, messageEntity);
		return addFlow;
	}	
}
