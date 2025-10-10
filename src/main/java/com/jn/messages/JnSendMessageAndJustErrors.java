package com.jn.messages;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.CcpEntity;
import com.jn.entities.JnEntityJobsnowWarning;

public class JnSendMessageAndJustErrors extends JnSendMessageToUser{

	
	public JnSendMessageToUser addOneStep(Function<CcpJsonRepresentation, CcpJsonRepresentation> step, CcpEntity parameterEntity, CcpEntity messageEntity) {
		Function<CcpJsonRepresentation, CcpJsonRepresentation> process = values -> {
			try {
				CcpJsonRepresentation apply = step.apply(values);
				return apply;
			} catch (Exception e) {
				CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
				JnEntityJobsnowWarning.ENTITY.createOrUpdate(errorDetails);
				e.printStackTrace();
				return values;
			}
		};
		JnSendMessageToUser addFlow = super.addOneStep(process, parameterEntity, messageEntity);
		return addFlow;
	}	
}
