package com.jn.entities;

import java.util.List;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldTransformer;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnBusinessNotifyError;
import com.jn.business.messages.JnBusinessSendUserToken;
import com.jn.entities.decorators.JnVersionableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDoNothing;
import com.jn.json.fields.validation.JnJsonCommonsFields;

@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityEmailParametersToSend.Fields.class)
/**
 * Armazena parâmetros de configuração para envio de emails: remetente, templateId, tipo de assunto
 * e parâmetros adicionais. Versionável, cache de 1 hora. Possui registros iniciais para os
 * contextos de notificação de erro ({@code JnBusinessNotifyError}) e envio de token de login
 * ({@code JnBusinessSendUserToken}).
 */
public class JnEntityEmailParametersToSend implements CcpEntityConfigurator{

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityEmailParametersToSend.class).entityInstance;
 
	public static enum Fields implements CcpJsonFieldName{
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpEntityFieldTransformer(JnJsonTransformersFieldsEntityDoNothing.class)
		email, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		sender, 
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		subjectType, 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		moreParameters, 
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		contentType
		;
	}

	public List<CcpBulkItem> getFirstRecordsToInsert() {
		String valorMais = "{" + "	\"email\": \"devs.jobsnow@gmail.com\",";
		String valorMaisMais = valorMais + "	\"sender\": \"devs.jobsnow@gmail.com\",";
		String valorMaisMaisMais = valorMaisMais
						+ "	\"subjectType\": \"";
						String name = JnBusinessNotifyError.class.getName();
						String valorMaisMaisMaisMais = valorMaisMaisMais
						+ name;
						String valorMaisMaisMaisMaisMais = valorMaisMaisMaisMais		
						+ "\",";
						String valorMaisMaisMaisMaisMaisMais = valorMaisMaisMaisMaisMais + "	\"templateId\": \"";
						String name2 = JnBusinessNotifyError.class.getName();
						String valorMaisMaisMaisMaisMaisMaisMais = valorMaisMaisMaisMaisMaisMais
						+ name2;
						String valorMaisMaisMaisMaisMaisMaisMaisMais = valorMaisMaisMaisMaisMaisMaisMais		
						+ "\"";
						String valorMaisMaisMaisMaisMaisMaisMaisMaisMais = valorMaisMaisMaisMaisMaisMaisMaisMais + "}";
						String valorMais2 = "{\"sender\": \"devs.jobsnow@gmail.com\"," + "	\"subjectType\": \"";
						String name3 = JnBusinessSendUserToken.class.getName();
						String valorMais2Mais = valorMais2
						+ name3;
						String valorMais2MaisMais = valorMais2Mais
						+ "\",";
						String valorMais2MaisMaisMais = valorMais2MaisMais
						+ "	\"templateId\": \"";
						String name4 = JnBusinessSendUserToken.class.getName();
						String valorMais2MaisMaisMaisMais = valorMais2MaisMaisMais
						+ name4;
						String valorMais2MaisMaisMaisMaisMais = valorMais2MaisMaisMaisMais
						+ "\",";
						String valorMais2MaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMais + "	\"moreParameters\": {";
						String valorMais2MaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMais
						+ "		\"linkedinAddress\": \"https://www.linkedin.com/in/onias85/\",";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMais
						+ "		\"linkedinName\": \"Onias\",";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMaisMais
						+ "		\"accessLink\": \"https://ccpjobsnow.com/#/tokenToSetPassword?email={email}&msgType=info&msgValue=newUser&token={token}\",";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMais
						+ "		\"telegramGroupLink\": \"https://t.me/joinchat/q_PRgF_18n00NjEx\",";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMais
						+ "		\"botAddress\": \"https://t.me/JnSuporteBot\"";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais + "	}";
						String valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais = valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais + "}";
						List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, 
						valorMaisMaisMaisMaisMaisMaisMaisMaisMais
						,
						valorMais2MaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMaisMais
				
				)
				;

		return createBulkItems;
	}
}
