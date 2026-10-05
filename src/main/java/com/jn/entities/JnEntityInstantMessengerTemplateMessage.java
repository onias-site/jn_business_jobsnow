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
import com.jn.entities.decorators.builders.JnEntityVersionablePurgeBuilder;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * Instant message templates by language and template id; the {@code message} supports template variables. Seeded for the error notification and for the pending and solved requests of token resend and unlock (sending is refused when the template does not exist).
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_instant_messenger_template_message}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityCustomDecorators(value = {@CcpEntityCustomDecorator(value = JnEntityVersionableBuilder.class, priority = 2),@CcpEntityCustomDecorator(value = JnEntityVersionablePurgeBuilder.class, priority = 5),})
@JnEntityVersionable(JnVersionableEntity.class)
@CcpEntityFieldsTransformer(classReferenceWithTheFields = JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntityInstantMessengerTemplateMessage.Fields.class)
public class JnEntityInstantMessengerTemplateMessage  implements CcpEntityConfigurator {

	/** The entity {@code jn_instant_messenger_template_message}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityInstantMessengerTemplateMessage.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code templateId} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		templateId,
		/** The {@code language} field: part of the primary key, validated as in {@code JnJsonCommonsFields}. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		language, 
		/** The {@code message} field: required, validated as in {@code JnJsonInstantMessengerFields}. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonCopyFieldValidationsFrom(JnJsonInstantMessengerFields.class)
		message
		;
	}
	/**
	 * Seeds, in Portuguese only, the templates of the error and warning notifications (full stack trace, one frame per line,
	 * since they are sent as files) and of the solved token unlock and resend requests.
	 * @return the seed records
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				// sent as a file (JnInstantMessageType.file), so the line breaks are real ones, and the stack trace is the
				// complete one, one frame per line (see JnMessages.JnNotifySupportAboutAnError)
				.put(JnJsonCommonsFields.message, "{type}\n\nError Description:\n{msg}\n\n{completeStackTrace}\n\nCaused by:\n{cause}");
				String name = JnMessages.JnNotifySupportAboutAnError.class.getName();
				
				CcpJsonRepresentation put2 = put
				.put(JnJsonCommonsFields.templateId, name);

				CcpJsonRepresentation notifyError = put2
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese)
		;
				CcpJsonRepresentation put7 = CcpOtherConstants.EMPTY_JSON
						.put(JnJsonCommonsFields.message, "{type}\n\nWarning Description:\n{msg}\n\n{completeStackTrace}\n\nCaused by:\n{cause}");
				String warningTemplateId = JnMessages.JnNotifySupportAboutWaring.class.getName();

				CcpJsonRepresentation put8 = put7
				.put(JnJsonCommonsFields.templateId, warningTemplateId);

				CcpJsonRepresentation notifyWarning = put8
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
				, notifyWarning
				, notifyAboutSolvedLockedToken
				, notifyAboutSolvedResendToken				
				);

		return createBulkItems;
	}

}
