package com.jn.business.commons;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.jn.utils.JnDeleteKeysFromCache;


public class JnBusinessSendEmailMessage implements CcpTopic{

	public static final JnBusinessSendEmailMessage INSTANCE = new JnBusinessSendEmailMessage();
	
	private JnBusinessSendEmailMessage() {	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		CcpEmailSender emailSender = CcpDependencyInjection.getDependency(CcpEmailSender.class);
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		
		CcpSelectUnionAll unionAll = crud.unionAll(json, JnDeleteKeysFromCache.INSTANCE, JnEntityEmailMessageSent.ENTITY, JnEntityEmailReportedAsSpam.ENTITY);
		
		boolean emailAlreadySent = JnEntityEmailMessageSent.ENTITY.isPresentInThisUnionAll(unionAll, json);
		
		if(emailAlreadySent) {
			return json;
		}

		boolean emailReportedAsSpam = JnEntityEmailReportedAsSpam.ENTITY.isPresentInThisUnionAll(unionAll, json);
		
		if(emailReportedAsSpam) {
			return json;
		}
		
		JnBusinessSendHttpRequest.INSTANCE.execute(json, x -> emailSender.send(x),JnBusinessHttpRequestType.email, JnEntityEmailParametersToSend.Fields.subjectType.name());
		JnEntityEmailMessageSent.ENTITY.createOrUpdate(json);
		return json;
	}

}
