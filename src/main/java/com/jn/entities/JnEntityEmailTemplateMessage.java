package com.jn.entities;

import java.util.List;
import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityDecorators;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityOperationSpecification;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntitySpecifications;
import com.ccp.especifications.db.utils.decorators.configurations.CcpEntityTransferOperationEspecification;
import com.ccp.especifications.db.utils.decorators.configurations.CcpIgnoreFieldsValidation;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityFactory;
import com.jn.business.commons.JnBusinessNotifyError;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.entities.decorators.JnEntityVersionable;
import com.jn.json.transformers.JnDefaultEntityFields;
import com.jn.utils.JnLanguage;

@CcpEntitySpecifications(
		classWithFieldsValidationsRules = CcpIgnoreFieldsValidation.class,
		inactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		reactivate = @CcpEntityTransferOperationEspecification(whenRecordToTransferIsFound = @CcpEntityOperationSpecification(afterOperation = {}), whenRecordToTransferIsNotFound = @CcpEntityOperationSpecification(afterOperation = {})),
		delete = @CcpEntityOperationSpecification(afterOperation = {}),
	    save = @CcpEntityOperationSpecification(afterOperation = {}),
		cacheableEntity = true
)
@CcpEntityDecorators(decorators = JnEntityVersionable.class)
public class JnEntityEmailTemplateMessage  implements CcpEntityConfigurator{

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailTemplateMessage.class).entityInstance;

	public static enum Fields implements CcpEntityField{
		templateId(true),language(true), subject(false), message(false)
		;
		
		private final boolean primaryKey;

		private final Function<CcpJsonRepresentation, CcpJsonRepresentation> transformer;
		
		private Fields(boolean primaryKey) {
			this(primaryKey, CcpOtherConstants.DO_NOTHING);
		}

		private Fields(boolean primaryKey, Function<CcpJsonRepresentation, CcpJsonRepresentation> transformer) {
			this.transformer = transformer;
			this.primaryKey = primaryKey;
		}
		
		public Function<CcpJsonRepresentation, CcpJsonRepresentation> getTransformer() {
			return this.transformer == CcpOtherConstants.DO_NOTHING ? JnDefaultEntityFields.getTransformer(this) : this.transformer;
		}
		public boolean isPrimaryKey() {
			return this.primaryKey;
		}

	}

	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, 
				"{"
				+ "	\"language\": \"" + JnLanguage.portuguese.name()
				+ "\","
				+ "	\"templateId\": \"" + JnBusinessSendUserToken.class.getName()
				+ "\","
				+ "	\"subject\": \"Envio de token para acesso ao sistema JobsNow\","
				+ "	\"message\": \"<html><head><style>p,h4{ font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;}#customers {  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;  border-collapse: collapse;  width: 100%;}#customers td, #customers th {  border: 1px solid #ddd;  padding: 8px;}#customers tr:nth-child(even){background-color: #f2f2f2;}#customers tr:hover {background-color: #ddd;}#customers th {  padding-top: 12px;  padding-bottom: 12px;  text-align: left;  background-color: #7FBCEC;  color: white;}</style></head><body><div><p>Saudações, caso você não me conheça ou não se lembre de mim, sou o <a href='{linkedinAddress}' target = '_blank'>{linkedinName}</a></p><p>Estou estou enviando um token para acesso ao sistema <a href='{accessLink}' target = '_blank'>JobsNow</a> esse token é {token} para você poder configurar seu usuário</p><p>Você pode receber vagas (condizentes com o teu perfil profissional cadastrado no site do JobsNow) ou currículos (de acordo com as vagas que você cadastrar) como notificação no seu celular <b>INSTANTÂNEAMENTE</b> se seguir os seguintes passos: </p><p>A) Instalar o telegram no seu celular </p><p>B) Procurar  na mensagem fixada <a href='{telegramGroupLink}' target = '_blank'>deste grupo de vagas</a> do telegram <a href = '{botAddress}' target = '_blank'> a este bot aqui</a> </p><p>C) Informar o e-mail  {recipient} para este bot</p><p>D) Depois de informar seu e-mail a este bot, informe o token {token} </p></div></body></html>\""
				+ "}",
				"{"
				+ "	\"language\": \""
				+ JnLanguage.portuguese.name()
				+ "\","
				+ "	\"templateId\": \"" + JnBusinessNotifyError.class.getName()
				+ "\","
				+ "	\"subject\": \"[ERROR] {type}\","
				+ "	\"message\": \"{type}<br/><br/><br/>Error Description:&nbsp;&nbsp;&nbsp;{msg}<br/><br/>{stackTrace}<br/><br/>Caused by:<br/>{cause}\""
				+ "}"
				
				);

		return createBulkItems;
	}
}
