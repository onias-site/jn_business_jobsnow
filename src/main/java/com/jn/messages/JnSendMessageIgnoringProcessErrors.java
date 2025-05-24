package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.commons.JnBusinessNotifySupport;
import com.jn.entities.JnEntityJobsnowWarning;

public class JnSendMessageIgnoringProcessErrors extends JnSendMessage{

	
	public JnSendMessage addOneStep(Function<CcpJsonRepresentation, CcpJsonRepresentation> step, CcpEntity parameterEntity, CcpEntity messageEntity) {
		Function<CcpJsonRepresentation, CcpJsonRepresentation> lenientProcess = values -> {
			try {
				CcpJsonRepresentation apply = step.apply(values);
				return apply;
			} catch (Exception e) {
				CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
				String name = JnBusinessNotifyError.class.getName();
				JnSendMessage x = new JnSendMessageAndJustErrors();
				JnBusinessNotifySupport.INSTANCE.apply(errorDetails, name, JnEntityJobsnowWarning.ENTITY, x);
				return values;
			}
		};
		JnSendMessage addFlow = super.addOneStep(lenientProcess, parameterEntity, messageEntity);
		return addFlow;
	}	
}
