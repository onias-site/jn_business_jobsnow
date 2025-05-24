package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.exceptions.http.CcpHttpError;
import com.jn.business.commons.JnBusinessSendEmailMessage;
import com.jn.business.commons.JnBusinessTryToSendInstantMessage;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.utils.JnDeleteKeysFromCache;


public class JnSendMessage {

	private final List<Function<CcpJsonRepresentation, CcpJsonRepresentation>> process = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>() ;
	
	private final List<CcpEntity> messageEntities = new ArrayList<>();
	
	public CreateStep createStep() {
		return new CreateStep(this);
	}
	
	public AddDefaultStep addDefaultProcessForEmailSending() {
		JnSendMessage addOneStep = this.addOneStep(JnBusinessSendEmailMessage.INSTANCE, JnEntityEmailParametersToSend.ENTITY, JnEntityEmailTemplateMessage.ENTITY);
		return new AddDefaultStep(addOneStep);
	}

	
	public AddDefaultStep addDefaultStepForTelegramSending() {
		JnSendMessage addOneStep = this.addOneStep(JnBusinessTryToSendInstantMessage.INSTANCE, JnEntityInstantMessengerParametersToSend.ENTITY, JnEntityInstantMessengerTemplateMessage.ENTITY);
		return new AddDefaultStep(addOneStep);
	}
	
	JnSendMessage addOneStep(Function<CcpJsonRepresentation, CcpJsonRepresentation> process, CcpEntity parameterEntity, CcpEntity messageEntity) {
		
		JnSendMessage getMessage = new JnSendMessage();
		
		getMessage.parameterEntities.addAll(this.parameterEntities);
		getMessage.messageEntities.addAll(this.messageEntities);
		getMessage.process.addAll(this.process);
		
		getMessage.parameterEntities.add(parameterEntity);
		getMessage.messageEntities.add(messageEntity);
		getMessage.process.add(process);
		
		return getMessage;
	}
	
	CcpJsonRepresentation executeAllSteps(String templateId, CcpEntity entityToSave, CcpJsonRepresentation entityValues, String languageToUseInErrorCases) {
		
		List<CcpEntity> allEntitiesToSearch = new ArrayList<>();
		allEntitiesToSearch.addAll(this.parameterEntities);
		allEntitiesToSearch.addAll(this.messageEntities);
		allEntitiesToSearch.add(entityToSave);
		
		CcpEntity[] entities = allEntitiesToSearch.toArray(new CcpEntity[allEntitiesToSearch.size()]);
		CcpJsonRepresentation idToSearch = entityValues
				.put(JnEntityEmailTemplateMessage.Fields.language.name(), languageToUseInErrorCases)
				.put(JnEntityEmailTemplateMessage.Fields.templateId.name(), templateId);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		
		CcpSelectUnionAll unionAll = crud.unionAll(idToSearch, JnDeleteKeysFromCache.INSTANCE, entities);
		
		boolean alreadySaved = entityToSave.isPresentInThisUnionAll(unionAll, idToSearch);
		
		if(alreadySaved) {
			return entityValues;
		}
		
		int k = 0;
		
		for (CcpEntity messageEntity : this.messageEntities) {
			
			CcpEntity parameterEntity = this.parameterEntities.get(k);
			
			CcpJsonRepresentation parameterData = parameterEntity.getRequiredEntityRow(unionAll, idToSearch);
			CcpJsonRepresentation moreParameters = parameterData.getInnerJson(JnEntityEmailParametersToSend.Fields.moreParameters.name());
			CcpJsonRepresentation allParameters = parameterData.removeField(JnEntityEmailParametersToSend.Fields.moreParameters.name()).putAll(moreParameters);
			CcpJsonRepresentation messageData = messageEntity.getRequiredEntityRow(unionAll, idToSearch);
			
			CcpJsonRepresentation allDataTogether = messageData.putAll(allParameters).putAll(entityValues);
			
 			Set<String> allFields = allDataTogether.fieldSet();
			
			CcpJsonRepresentation messageToSend = allDataTogether;
			
			for (String key : allFields) {
				messageToSend = messageToSend.putFilledTemplate(key, key);
			}
			Function<CcpJsonRepresentation, CcpJsonRepresentation> process = this.process.get(k);
			try {
				process.apply(messageToSend);
			} catch (CcpHttpError e) {

			}
			k++;
		}
		CcpJsonRepresentation renameField = entityValues.renameField("msg", "message");
		entityToSave.createOrUpdate(renameField);
		return entityValues;
	}
}
