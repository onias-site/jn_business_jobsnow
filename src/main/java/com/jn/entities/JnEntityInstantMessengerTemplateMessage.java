package com.jn.entities;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages.JnBusinessNotifyError;
import com.jn.business.messages.JnMessages.NotifySupportAboutPendingResendLoginToken;
import com.jn.business.messages.JnMessages.NotifySupportAboutPendingUnlockLoginToken;
import com.jn.entities.decorators.JnVersionableEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerTemplateMessage.Fields.class)
/**
 * Armazena templates de mensagens instantâneas por idioma e {@code templateId}. O campo
 * {@code message} suporta variáveis de template. Versionável, cache de 1 hora. Possui registro
 * inicial para o template de notificação de erro em português.
 */
public class JnEntityInstantMessengerTemplateMessage  implements CcpEntityConfigurator {

	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerTemplateMessage.class).entityInstance;

	public static enum Fields implements CcpJsonFieldName{
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		templateId,
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language, 
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message
		;
	}
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		
		CcpJsonRepresentation notifyError = CcpOtherConstants.EMPTY_JSON
				.put(Fields.message, "{type}\\n\\nError Description:\n {msg}\\n\\n{stackTrace}\\n\\nCaused by:\\n{cause}")
				.put(Fields.templateId, JnBusinessNotifyError.class.getName())
				.put(Fields.language, JnLanguage.portuguese)
		;

		CcpJsonRepresentation notifyUnlockToken = CcpOtherConstants.EMPTY_JSON
				.put(Fields.message, "Ao endereço {email}, envie a seguinte mensagem:\n\n\nVocê solicitou o desbloqueio de seu token para (re)cadastro / desbloqueio de senha. Atendendo ao seu pedido então, a senha para o desbloqueio do seu token é {password}, o token que você deve informar no campo de token é {token}")
				.put(Fields.templateId, NotifySupportAboutPendingUnlockLoginToken.class.getName())
				.put(Fields.language, JnLanguage.portuguese)
		;

		CcpJsonRepresentation notifyResendToken = CcpOtherConstants.EMPTY_JSON
				.put(Fields.message, "Ao endereço {email}, envie a seguinte mensagem:\n\n\nVocê solicitou o reenvio de seu token para (re)cadastro / desbloqueio de senha. Atendendo ao seu pedido então, o token que você deve informar no campo de token é {token}")
				.put(Fields.templateId, NotifySupportAboutPendingResendLoginToken.class.getName())
				.put(Fields.language, JnLanguage.portuguese)
		;
		
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(ENTITY, notifyError, notifyUnlockToken, notifyResendToken);

		return createBulkItems;
	}

}
