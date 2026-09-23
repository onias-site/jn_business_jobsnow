package com.jn.entities;

import com.jn.entities.decorators.annotations.JnEntityVersionable;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCustomDecorators;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.business.messages.JnMessages;
import com.jn.entities.decorators.builders.JnEntityVersionableBuilder;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerTemplateMessage.Fields.class)
/**
 * Armazena templates de mensagens instantâneas por idioma e {@code templateId}. O campo
 * {@code message} suporta variáveis de template. Versionável, cache de 1 hora. Possui registros
 * iniciais em português para a notificação de erro e para os pedidos de reenvio e de desbloqueio de
 * token, tanto pendentes quanto atendidos — cada um deles precisa do template correspondente, pois o
 * envio é recusado quando o template não existe.
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
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.message, "{type}\\n\\nError Description:\n {msg}\\n\\n{stackTrace}\\n\\nCaused by:\\n{cause}");
				String name = JnMessages.JnNotifySupportAboutAnError.class.getName();
				
				CcpJsonRepresentation put2 = put
				.put(JnJsonCommonsFields.templateId, name);

				CcpJsonRepresentation notifyError = put2
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese)
		;
				
				CcpJsonRepresentation put3 = CcpOtherConstants.EMPTY_JSON
						.put(JnJsonCommonsFields.message, "Ao endereço {email}, envie a seguinte mensagem:\n\n\nVocê solicitou o desbloqueio de seu token para (re) cadastro / desbloqueio de senha. Atendendo ao seu pedido. O token que você deve informar no campo de token é {token}");
						String name2 = JnMessages.JnNotifySupportAboutSolvedLockedLoginToken.class.getName();
						CcpJsonRepresentation put4 = put3
						.put(JnJsonCommonsFields.templateId, name2);

						CcpJsonRepresentation notifyAboutSolvedLockedToken = put4
						.put(JnJsonCommonsFields.language, JnLanguage.portuguese)
				;
				CcpJsonRepresentation put5 = CcpOtherConstants.EMPTY_JSON
						.put(JnJsonCommonsFields.message, "Ao endereço {email}, envie a seguinte mensagem:\n\n\nVocê solicitou o reenvio de seu token para (re) cadastro / desbloqueio de senha. Atendendo ao seu pedido, o token que você deve informar no campo de token é {token}");
						String name3 = JnMessages.JnNotifySupportAboutSolvedResendLoginToken.class.getName();
						CcpJsonRepresentation put6 = put5
						.put(JnJsonCommonsFields.templateId, name3);

						CcpJsonRepresentation notifyAboutSolvedResendToken = put6
						.put(JnJsonCommonsFields.language, JnLanguage.portuguese)
				;
				
		List<CcpBulkItem> createBulkItems = CcpEntityConfigurator.super.toCreateBulkItems(
				ENTITY
				, notifyError
				, notifyAboutSolvedLockedToken
				, notifyAboutSolvedResendToken				
				);

		return createBulkItems;
	}

}
