package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.http.CcpErrorHttp;
import com.jn.business.commons.JnBusinessSendEmailMessage;
import com.jn.business.commons.JnBusinessTryToSendInstantMessage;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnSendMessageToUser {
	enum JsonFieldNames implements CcpJsonFieldName{
		message, msg
	}

	private final List<Function<CcpJsonRepresentation, CcpJsonRepresentation>> process = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>() ;
	
	private final List<CcpEntity> messageEntities = new ArrayList<>();
	
	public JnCreateStep createStep() {
		return new JnCreateStep(this);
	}
	
	public JnAddDefaultStep addDefaultProcessForEmailSending() {
		JnSendMessageToUser addOneStep = this.addOneStep(JnBusinessSendEmailMessage.INSTANCE, JnEntityEmailParametersToSend.ENTITY, JnEntityEmailTemplateMessage.ENTITY);
		return new JnAddDefaultStep(addOneStep);
	}

	
	public JnAddDefaultStep addDefaultStepForTelegramSending() {
		JnSendMessageToUser addOneStep = this.addOneStep(JnBusinessTryToSendInstantMessage.INSTANCE, JnEntityInstantMessengerParametersToSend.ENTITY, JnEntityInstantMessengerTemplateMessage.ENTITY);
		return new JnAddDefaultStep(addOneStep);
	}
	
	JnSendMessageToUser addOneStep(Function<CcpJsonRepresentation, CcpJsonRepresentation> process, CcpEntity parameterEntity, CcpEntity messageEntity) {
		
		JnSendMessageToUser getMessage = new JnSendMessageToUser();
		
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
				.put(JnEntityEmailTemplateMessage.Fields.language, languageToUseInErrorCases)
				.put(JnEntityEmailTemplateMessage.Fields.templateId, templateId);
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
			CcpJsonRepresentation moreParameters = parameterData.getInnerJson(JnEntityEmailParametersToSend.Fields.moreParameters);
			CcpJsonRepresentation allParameters = parameterData.removeField(JnEntityEmailParametersToSend.Fields.moreParameters).putAll(moreParameters);
			CcpJsonRepresentation messageData = messageEntity.getRequiredEntityRow(unionAll, idToSearch);
			
			CcpJsonRepresentation allDataTogether = messageData.putAll(allParameters).putAll(entityValues);
			
 			Set<String> allFields = allDataTogether.fieldSet();
			
			CcpJsonRepresentation messageToSend = allDataTogether;
			
			for (String key : allFields) {
				messageToSend = messageToSend.getDynamicVersion().putFilledTemplate(key, key);
			}
			Function<CcpJsonRepresentation, CcpJsonRepresentation> process = this.process.get(k);
			try {
				process.apply(messageToSend);
			} catch (CcpErrorHttp e) {
				e.printStackTrace();
			}
			k++;
		}
		CcpJsonRepresentation renameField = entityValues.renameField(JsonFieldNames.msg, JsonFieldNames.message);
		entityToSave.createOrUpdate(renameField);
		return entityValues;
	}
}
