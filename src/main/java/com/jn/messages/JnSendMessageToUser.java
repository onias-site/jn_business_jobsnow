package com.jn.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnBusinessSendEmailMessage;
import com.jn.business.messages.JnBusinessSendInstantMessage;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.JnEntityEmailReportedAsSpam;
import com.jn.entities.JnEntityEmailTemplateMessage;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnSendMessageToUser {
	

	private final List<JnBusinessSendHttpRequest> messengers = new ArrayList<>();

	private final List<CcpEntity> alreadySentEntities = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>();

	private final List<CcpEntity> messageEntities = new ArrayList<>();

	private final List<CcpEntity> blockEntities = new ArrayList<>();

	public JnCreateStep createStep() {
		JnCreateStep jnCreateStep = new JnCreateStep(this);
		return jnCreateStep;
	}

	public JnAddDefaultStep addDefaultProcessToEmailSending(JnMessageSenderExceptionHandler exceptionHandler) {
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnBusinessSendEmailMessage.INSTANCE, exceptionHandler);
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester,
				JnEntityEmailParametersToSend.ENTITY,
				JnEntityEmailTemplateMessage.ENTITY,
				JnEntityEmailReportedAsSpam.ENTITY,
				JnEntityEmailMessageSent.ENTITY
		);
		JnAddDefaultStep jnAddDefaultStep = new JnAddDefaultStep(addOneStep);
		return jnAddDefaultStep;
	}

	public JnAddDefaultStep addDefaultStepToInstantMessageSending(JnMessageSenderExceptionHandler exceptionHandler) {
		JnBusinessSendHttpRequest httpRequester = new JnBusinessSendHttpRequest(JnBusinessSendInstantMessage.INSTANCE, exceptionHandler);
		JnSendMessageToUser addOneStep = this.addOneStep(
				httpRequester,
				JnEntityInstantMessengerParametersToSend.ENTITY,
				JnEntityInstantMessengerTemplateMessage.ENTITY,
				JnEntityInstantMessengerBotLocked.ENTITY,
				JnEntityInstantMessengerMessageSent.ENTITY
		);
		JnAddDefaultStep jnAddDefaultStep2 = new JnAddDefaultStep(addOneStep);
		return jnAddDefaultStep2;
	}

	JnSendMessageToUser addOneStep(JnBusinessSendHttpRequest messenger, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
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


	
	
	@SuppressWarnings("serial")
	static class CcpMessageDidNotSend extends RuntimeException{
		
		public final CcpJsonRepresentation jsonToSave;
		
		public CcpMessageDidNotSend(JnSendMessageToUser obj, MustNotSendMessage reason, CcpJsonRepresentation json, Integer index) {
			List<CcpEntity> entities = reason.getEntities(obj);
			CcpEntity entity = entities.get(index);
			CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
			String reasonType = reason.name();
			CcpJsonRepresentation put = json
					.put(JnEntityMessageDidNotSent.Fields.reasonType,  entityMetaData.entityName);

					this.jsonToSave = put
					.put(JnEntityMessageDidNotSent.Fields.reasonDescription,  reasonType)
									;
		}
	}
	
	CcpJsonRepresentation executeAllSteps(String templateId, CcpEntity entityToSave, CcpJsonRepresentation entityValues, String languageToUseInErrorCases) {
		
		List<CcpEntity> allEntitiesToSearch = new ArrayList<>();
		
		allEntitiesToSearch.addAll(this.alreadySentEntities);
		allEntitiesToSearch.addAll(this.parameterEntities);
		allEntitiesToSearch.addAll(this.messageEntities);
		allEntitiesToSearch.addAll(this.blockEntities);
		
		allEntitiesToSearch.add(entityToSave);
		int allEntitiesToSearchSize = allEntitiesToSearch.size();

		CcpEntity[] entities = allEntitiesToSearch.toArray(new CcpEntity[allEntitiesToSearchSize]);
		CcpJsonRepresentation put2 = entityValues
				.put(JnJsonCommonsFields.language, languageToUseInErrorCases);

				CcpJsonRepresentation idToSearch = put2
				.put(JnJsonCommonsFields.templateId, templateId);
		
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		
		CcpSelectUnionAll unionAll = crud.unionAll(idToSearch, JnDeleteKeysFromCache.INSTANCE, entities);

		boolean alreadySaved = entityToSave.isPresentInThisUnionAll(unionAll, idToSearch);
		if (alreadySaved) {
			return entityValues;
		}
		
		boolean messageSent = false;
		for (int index = 0; index < this.alreadySentEntities.size(); index++) {
			try {
				MustNotSendMessage.validate(this, unionAll, idToSearch , index);
			} catch (CcpMessageDidNotSend e) {
				JnEntityMessageDidNotSent.ENTITY.save(e.jsonToSave);
				continue;
			}
			
			JnBusinessSendHttpRequest messenger = this.messengers.get(index);
			CcpJsonRepresentation result = this.sendMessage(unionAll, idToSearch, index);
			Class<? extends CcpBusiness> class1 = messenger.processThatSendsHttpRequest.getClass();
			String simpleName = class1.getSimpleName();
			CcpFieldName ccpFieldName = new CcpFieldName(simpleName);
			idToSearch = idToSearch.put(ccpFieldName, result);
			messageSent = true;
		}
		
		if(messageSent) {
			entityToSave.save(idToSearch);
		}

		return entityValues;
	}

	private CcpJsonRepresentation sendMessage(CcpSelectUnionAll unionAll, CcpJsonRepresentation json, int index) {

		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		
		CcpBusiness messenger                        = this.messengers.get(index);
		CcpEntity messageEntity                      = this.messageEntities.get(index);
		CcpEntity parameterEntity                    = this.parameterEntities.get(index);

		CcpJsonRepresentation messageData  = messageEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		
		boolean doesNotSendThisMessageType = messageData.isEmpty();
		
		if (doesNotSendThisMessageType) {
			return json;
		}

		CcpJsonRepresentation parameterData        = parameterEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation moreParameters       = parameterData.getInnerJson(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation removeFields         = parameterData.removeFields(JnJsonCommonsFields.moreParameters);
		CcpJsonRepresentation allParameters        = removeFields.mergeWithAnotherJson(moreParameters);
		CcpJsonRepresentation mergeWithAnotherJson = messageData.mergeWithAnotherJson(allParameters);
		CcpJsonRepresentation message              = mergeWithAnotherJson.mergeWithAnotherJson(json);
		CcpJsonRepresentation result               = messenger.execute(message);

		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		
		alreadySentEntity.save(result);
		
		return result;
	}

	// ─── Fluent-API step classes ──────────────────────────────────────────────





















}
