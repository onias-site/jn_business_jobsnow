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
import com.ccp.especifications.db.crud.CcpGetEntityId.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData.CcpErrorEntityPrimaryKeyIsMissing;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnBusinessNotifyError;
import com.jn.business.messages.JnBusinessNotifySupport;
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
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnSendMessageToUser {

	enum JsonFieldNames implements CcpJsonFieldName {
		message, msg
	}

	private final List<CcpBusiness> messengers = new ArrayList<>();

	private final List<CcpEntity> alreadySentEntities = new ArrayList<>();

	private final List<CcpEntity> parameterEntities = new ArrayList<>();

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
		if (alreadySaved) {
			return entityValues;
		}

		for (int index = 0; index < this.alreadySentEntities.size(); index++) {
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
	private boolean mustSkip(
			CcpSelectUnionAll unionAll,
			CcpJsonRepresentation json,
			Integer index,
			Function<Integer, CcpEntity>... functions
	) {
		Map<CcpEntity, boolean[]> decisions = new HashMap<>();

		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		CcpEntity parameterEntity   = this.parameterEntities.get(index);
		CcpEntity messageEntity     = this.messageEntities.get(index);
		CcpEntity blockEntity       = this.blockEntities.get(index);

		decisions.put(alreadySentEntity, new boolean[]{true,  false});
		decisions.put(parameterEntity,   new boolean[]{false, false});
		decisions.put(messageEntity,     new boolean[]{false, false});
		decisions.put(blockEntity,       new boolean[]{true,  true });

		for (Function<Integer, CcpEntity> function : functions) {
			CcpEntity entity   = function.apply(index);
			boolean[] booleans = decisions.get(entity);
			try {
				boolean skip     = entity.isPresentInThisUnionAll(unionAll, json);
				boolean decision = booleans[0];
				boolean mustSkip = decision == skip;
				if (mustSkip) {
					return true;
				}
			} catch (CcpErrorEntityPrimaryKeyIsMissing e) {
				boolean mustSkip = booleans[1];
				if (mustSkip) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private CcpJsonRepresentation sendMessage(CcpSelectUnionAll unionAll, CcpJsonRepresentation json, int index) {
		boolean mustSkip = this.mustSkip(
				unionAll,
				json,
				index,
				idx -> this.alreadySentEntities.get(idx),
				idx -> this.parameterEntities.get(idx),
				idx -> this.messageEntities.get(idx),
				idx -> this.blockEntities.get(idx)
		);

		if (mustSkip) {
			return json;
		}

		Supplier<CcpJsonRepresentation> jsonSupplier = json.getJsonSupplier();
		CcpEntity messageEntity   = this.messageEntities.get(index);
		CcpBusiness messenger     = this.messengers.get(index);
		CcpEntity parameterEntity = this.parameterEntities.get(index);
		CcpJsonRepresentation parameterData = parameterEntity.getRecordFromUnionAll(unionAll, jsonSupplier);

		boolean doesNotSendThisMessageType = parameterData.isEmpty();
		if (doesNotSendThisMessageType) {
			return json;
		}

		CcpJsonRepresentation moreParameters    = parameterData.getInnerJson(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation removeFields      = parameterData.removeFields(JnEntityEmailParametersToSend.Fields.moreParameters);
		CcpJsonRepresentation messageData       = messageEntity.getRecordFromUnionAll(unionAll, jsonSupplier);
		CcpJsonRepresentation allParameters     = removeFields.mergeWithAnotherJson(moreParameters);
		CcpJsonRepresentation mergeWithAnotherJson = messageData.mergeWithAnotherJson(allParameters);
		CcpJsonRepresentation message           = mergeWithAnotherJson.mergeWithAnotherJson(json);

		CcpJsonRepresentation result = messenger.execute(message);

		CcpEntity alreadySentEntity = this.alreadySentEntities.get(index);
		alreadySentEntity.save(result);
		return result;
	}

	// ─── Fluent-API step classes ──────────────────────────────────────────────

	public static class JnAddDefaultStep {

		final JnSendMessageToUser getMessage;

		JnAddDefaultStep(JnSendMessageToUser getMessage) {
			this.getMessage = getMessage;
		}

		public JnCreateStep andCreateAnotherStep() {
			return new JnCreateStep(this.getMessage);
		}

		public JnSoWithAllAddedStepsAnd soWithAllAddedProcessAnd() {
			return new JnSoWithAllAddedStepsAnd(this.getMessage);
		}

		public JnSendMessageToUser and() {
			return this.getMessage;
		}
	}

	public static class JnCreateStep {

		final JnSendMessageToUser getMessage;

		JnCreateStep(JnSendMessageToUser getMessage) {
			this.getMessage = getMessage;
		}

		public JnWithTheProcess withTheProcess(CcpBusiness process) {
			return new JnWithTheProcess(this, process);
		}
	}

	public static class JnWithTheProcess {

		final JnCreateStep createStep;

		final CcpBusiness process;

		public JnWithTheProcess(JnCreateStep createStep, CcpBusiness process) {
			this.createStep = createStep;
			this.process    = process;
		}

		public JnAndWithTheParametersEntity andWithTheParametersEntity(CcpEntity parametersEntity) {
			return new JnAndWithTheParametersEntity(this, parametersEntity);
		}
	}

	public static class JnAndWithTheParametersEntity {

		final JnWithTheProcess withProcess;

		final CcpEntity parametersEntity;

		JnAndWithTheParametersEntity(JnWithTheProcess withProcess, CcpEntity parametersEntity) {
			this.withProcess       = withProcess;
			this.parametersEntity  = parametersEntity;
		}

		public JnAndWithTheTemplateEntity andWithTheTemplateEntity(CcpEntity templateEntity) {
			return new JnAndWithTheTemplateEntity(this, templateEntity);
		}
	}

	public static class JnAndWithTheTemplateEntity {

		final JnAndWithTheParametersEntity andWithParametersEntity;

		final CcpEntity templateEntity;

		JnAndWithTheTemplateEntity(JnAndWithTheParametersEntity andWithParametersEntity, CcpEntity templateEntity) {
			this.andWithParametersEntity = andWithParametersEntity;
			this.templateEntity          = templateEntity;
		}

		public JnCreateStep andCreateAnotherStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
			this.addStep(blockEntity, alreadySentEntity);
			return new JnCreateStep(this.andWithParametersEntity.withProcess.createStep.getMessage);
		}

		private JnAndWithTheTemplateEntity addStep(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
			this.andWithParametersEntity.withProcess.createStep.getMessage
					.addOneStep(this.andWithParametersEntity.withProcess.process, this.andWithParametersEntity.parametersEntity, this.templateEntity, blockEntity, alreadySentEntity);
			return this;
		}

		public JnSoWithAllAddedStepsAnd soWithAllAddedStepsAnd(CcpEntity blockEntity, CcpEntity alreadySentEntity) {
			this.addStep(blockEntity, alreadySentEntity);
			return new JnSoWithAllAddedStepsAnd(this.andWithParametersEntity.withProcess.createStep.getMessage);
		}
	}

	public static class JnSoWithAllAddedStepsAnd {

		final JnSendMessageToUser getMessage;

		JnSoWithAllAddedStepsAnd(JnSendMessageToUser getMessage) {
			this.getMessage = getMessage;
		}

		public JnWithTheTemplateId withTheTemplateEntity(String templateId) {
			return new JnWithTheTemplateId(this, templateId);
		}
	}

	public static class JnWithTheTemplateId {

		final JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps;

		final String templateId;

		JnWithTheTemplateId(JnSoWithAllAddedStepsAnd soExecuteAllAddedSteps, String templateId) {
			this.soExecuteAllAddedSteps = soExecuteAllAddedSteps;
			this.templateId             = templateId;
		}

		public JnAndWithTheEntityToBlockMessageResend andWithTheEntityToBlockMessageResend(CcpEntity entityToSave) {
			return new JnAndWithTheEntityToBlockMessageResend(this, entityToSave);
		}
	}

	public static class JnAndWithTheEntityToBlockMessageResend {

		final JnWithTheTemplateId withTemplateId;

		final CcpEntity entityToSave;

		JnAndWithTheEntityToBlockMessageResend(JnWithTheTemplateId withTemplateId, CcpEntity entityToSave) {
			this.withTemplateId = withTemplateId;
			this.entityToSave   = entityToSave;
		}

		public JnAndWithTheJsonValues andWithTheMessageValuesFromJson(CcpJsonRepresentation jsonValues) {
			return new JnAndWithTheJsonValues(this, jsonValues);
		}
	}

	public static class JnAndWithTheJsonValues {

		final JnAndWithTheEntityToBlockMessageResend andWithEntityToSave;

		final CcpJsonRepresentation jsonValues;

		JnAndWithTheJsonValues(JnAndWithTheEntityToBlockMessageResend andWithEntityToSave, CcpJsonRepresentation jsonValues) {
			this.andWithEntityToSave = andWithEntityToSave;
			this.jsonValues          = jsonValues;
		}

		public JnAndWithTheSupportLanguage andWithTheSupportLanguage(String supportLanguage) {
			return new JnAndWithTheSupportLanguage(this, supportLanguage);
		}
	}

	public static class JnAndWithTheSupportLanguage {

		final JnAndWithTheJsonValues andWithJsonValues;

		final String supportLanguage;

		JnAndWithTheSupportLanguage(JnAndWithTheJsonValues andWithJsonValues, String supportLanguage) {
			this.andWithJsonValues = andWithJsonValues;
			this.supportLanguage   = supportLanguage;
		}

		public CcpJsonRepresentation sendAllMessages() {
			return this.andWithJsonValues.andWithEntityToSave.withTemplateId.soExecuteAllAddedSteps.getMessage
					.executeAllSteps(
							this.andWithJsonValues.andWithEntityToSave.withTemplateId.templateId,
							this.andWithJsonValues.andWithEntityToSave.entityToSave,
							this.andWithJsonValues.jsonValues,
							this.supportLanguage
					);
		}
	}

	public static class JnSendMessageAndJustErrors extends JnSendMessageToUser {

		public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
			CcpBusiness process = values -> {
				try {
					return step.apply(values);
				} catch (Exception e) {
					CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
					JnEntityJobsnowWarning.ENTITY.save(errorDetails);
					e.printStackTrace();
					return values;
				}
			};
			return super.addOneStep(process, parameterEntity, messageEntity, blockEntity, alreadySentEntity);
		}
	}

	public static class JnSendMessageIgnoringProcessErrors extends JnSendMessageToUser {

		public JnSendMessageToUser addOneStep(CcpBusiness step, CcpEntity parameterEntity, CcpEntity messageEntity, CcpEntity blockEntity, CcpEntity alreadySentEntity) {
			CcpBusiness lenientProcess = values -> {
				try {
					return step.apply(values);
				} catch (Exception e) {
					CcpJsonRepresentation errorDetails = new CcpJsonRepresentation(e);
					String name = JnBusinessNotifyError.class.getName();
					JnSendMessageToUser x = new JnSendMessageAndJustErrors();
					JnBusinessNotifySupport.INSTANCE.apply(errorDetails, name, JnEntityJobsnowWarning.ENTITY, x);
					return values;
				}
			};
			return super.addOneStep(lenientProcess, parameterEntity, messageEntity, blockEntity, alreadySentEntity);
		}
	}
}
