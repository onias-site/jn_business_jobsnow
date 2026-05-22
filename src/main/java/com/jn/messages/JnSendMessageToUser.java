package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnBusinessSendEmailMessage;
import com.jn.business.messages.JnBusinessSendInstantMessage;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnSendMessageToUser {
	enum JsonFieldNames implements CcpJsonFieldName{
		message, msg
	}

	private final List<CcpBusiness> messengers = new ArrayList<>();

	private final List<CcpEntity> alreadySentEntities = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>() ;
	
	private final List<CcpEntity> messageEntities = new ArrayList<>();

	private final List<CcpEntity> blockEntities = new ArrayList<>();

	public JnCreateStep createStep() {
		return new JnCreateStep(this);
	}
	
	public JnAddDefaultStep addDefaultProcessToEmailSending() {
		
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnBusinessSendEmailMessage.INSTANCE);
		
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester, 
				JnEntityEmailParametersToSend.ENTITY, 
				JnEntityEmailTemplateMessage.ENTITY,
				JnEntityEmailReportedAsSpam.ENTITY,
				JnEntityEmailMessageSent.ENTITY
				);
		return new JnAddDefaultStep(addOneStep);
	}

	
	public JnAddDefaultStep addDefaultStepToInstantMessageSending() {
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnBusinessSendInstantMessage.INSTANCE);
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester, 
				JnEntityInstantMessengerParametersToSend.ENTITY, 
				JnEntityInstantMessengerTemplateMessage.ENTITY,
				JnEntityInstantMessengerBotLocked.ENTITY,
				JnEntityInstantMessengerMessageSent.ENTITY
				);
		return new JnAddDefaultStep(addOneStep);
	}
	
	JnSendMessageToUser addOneStep(CcpBusiness messenger, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
		
		JnSendMessageToUser getMessage = new JnSendMessageToUser();
		
		getMessage.alreadySentEntities.addAll(this.alreadySentEntities);
		getMessage.parameterEntities.addAll(this.parameterEntities);
		getMessage.messageEntities.addAll(this.messageEntities);
		getMessage.blockEntities.addAll(this.blockEntities);
		getMessage.messengers.addAll(this.messengers);
		
		getMessage.alreadySentEntities.add(alreadySentEntity);
		getMessage.parameterEntities.add(parameterEntity);
		getMessage.messageEntities.add(messageEntity);
		getMessage.blockEntities.add(blockEntity);
		getMessage.messengers.add(messenger);
		
		return getMessage;
	}
	
	CcpJsonRepresentation executeAllSteps(String templateId, CcpEntity entityToSave, CcpJsonRepresentation entityValues, String languageToUseInErrorCases) {
		
		List<CcpEntity> allEntitiesToSearch = new ArrayList<>();
		allEntitiesToSearch.addAll(this.alreadySentEntities);
		allEntitiesToSearch.addAll(this.parameterEntities);
		allEntitiesToSearch.addAll(this.messageEntities);
		allEntitiesToSearch.addAll(this.blockEntities);
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
		
		for(int index = 0; index < this.alreadySentEntities.size(); index++) {
			CcpBusiness messenger = this.messengers.get(index);
			CcpJsonRepresentation result = this.sendMessage(unionAll, idToSearch, index);
			idToSearch = idToSearch.put(() -> messenger.getClass().getSimpleName(), result);
		}
		entityToSave.save(idToSearch);
		return entityValues;
	}
	
	
	private CcpJsonRepresentation sendMessage(CcpSelectUnionAll unionAll, CcpJsonRepresentation json, int index) {
		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		boolean alreadySent = alreadySentEntity.isPresentInThisUnionAll(unionAll, json);
		if(alreadySent) {
			return json;
		}
		CcpEntity blockEntity = this.blockEntities.get(index);
		boolean blocked = blockEntity.isPresentInThisUnionAll(unionAll, json);
		if(blocked) {
			return json;
		}
		CcpEntity parameterEntity = this.parameterEntities.get(index);
		CcpEntity messageEntity = this.messageEntities.get(index);
		CcpBusiness messenger = this.messengers.get(index);
		
		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		CcpJsonRepresentation parameterData = parameterEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation moreParameters = parameterData.getInnerJson(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation removeFields = parameterData.removeFields(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation messageData = messageEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation allParameters = removeFields.mergeWithAnotherJson(moreParameters);
		CcpJsonRepresentation message = messageData.mergeWithAnotherJson(allParameters);
		
		CcpJsonRepresentation result = messenger.execute(message);
		
		return result;
		
	}
}
