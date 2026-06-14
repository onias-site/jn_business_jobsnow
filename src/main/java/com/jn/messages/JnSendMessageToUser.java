package com.jn.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorEntityPrimaryKeyIsMissing;
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

/**
 * Classe principal do builder fluent de envio de mensagens. Mantém listas paralelas de remetentes,
 * entidades de parâmetros, templates, bloqueio e controle de já-enviado. Ao executar
 * {@code executeAllSteps}, faz um {@code unionAll} para buscar todos os dados necessários de uma
 * vez, verifica já-enviado e, para cada canal, verifica bloqueio antes de enviar.
 */
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
			Class<? extends CcpBusiness> class1 = messenger.getClass();
			String simpleName = class1.getSimpleName();
			idToSearch = idToSearch.put(new CcpFieldName(simpleName), result);
		}
		entityToSave.save(idToSearch);
		return entityValues;
	}
	
	
	@SuppressWarnings("unchecked")
	private boolean mustSkip(CcpSelectUnionAll unionAll, 
			CcpJsonRepresentation json,
			Integer index,
			Function<Integer, CcpEntity>... functions
			
			) {
				Map<CcpEntity, boolean[]> decisions = new HashMap<CcpEntity, boolean[]>();
				
				CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
				CcpEntity parameterEntity = this.parameterEntities.get(index);
				CcpEntity messageEntity = this.messageEntities.get(index);
				CcpEntity blockEntity = this.blockEntities.get(index);
				
				decisions.put(alreadySentEntity, new boolean[] {true, false});
				decisions.put(parameterEntity, new boolean[] {false, false});
				decisions.put(messageEntity, new boolean[] {false, false});
				decisions.put(blockEntity, new boolean[] {true, true});
		
				for (Function<Integer, CcpEntity> function : functions) {

					CcpEntity entity = function.apply(index);
					boolean[] booleans = decisions.get(entity);

					try {
						boolean skip = entity.isPresentInThisUnionAll(unionAll, json);
						boolean decision = booleans[0];
						boolean mustSkip = decision == skip;
						if(mustSkip) {
							return true;
						}

					} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
						boolean mustSkip = booleans[1];
						if(mustSkip) {
							return true;
						}
					}
				}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private CcpJsonRepresentation sendMessage(
			CcpSelectUnionAll unionAll, 
			CcpJsonRepresentation json, 
			int index
			) {
		
		boolean mustSkip = this.mustSkip(
				unionAll, 
				json, 
				index 
				,idx -> this.alreadySentEntities.get(idx)
				,idx -> this.parameterEntities.get(idx)
				,idx -> this.messageEntities.get(idx)
				,idx -> this.blockEntities.get(idx)
				);
		
		if(mustSkip) {
			return json;
		}
		
		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		CcpEntity messageEntity = this.messageEntities.get(index);
		CcpBusiness messenger = this.messengers.get(index);
		CcpEntity parameterEntity = this.parameterEntities.get(index);
		CcpJsonRepresentation parameterData = parameterEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		
		boolean doesNotSendThisMessageType = parameterData.isEmpty();
		if(doesNotSendThisMessageType) {
			return json;
		}

		CcpJsonRepresentation moreParameters = parameterData.getInnerJson(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation removeFields = parameterData.removeFields(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation messageData = messageEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation allParameters = removeFields.mergeWithAnotherJson(moreParameters);
		CcpJsonRepresentation mergeWithAnotherJson = messageData.mergeWithAnotherJson(allParameters);
		CcpJsonRepresentation message = mergeWithAnotherJson.mergeWithAnotherJson(json);
		
		CcpJsonRepresentation result = messenger.execute(message);
		
		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		alreadySentEntity.save(json);
		return result;
	}
	
}
