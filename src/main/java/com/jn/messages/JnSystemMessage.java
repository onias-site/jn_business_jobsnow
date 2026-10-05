package com.jn.messages;

import java.util.Arrays;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntitySystemMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;

/**
 * Enum whose items are texts shown to the end user, stored in {@link JnEntitySystemMessage} (one record per item
 * and language) instead of written as literals in the code. The records are seeded by the
 * {@code getFirstRecordsToInsert} of an entity of the cost center that owns the texts, through
 * {@link #toBulkItems(JnLanguage, String)}.
 *
 * <p>A text may be a template: each {@code {field}} is replaced by the value of that field in the parameters
 * given to {@link #getMessage(JnLanguage, CcpJsonRepresentation)}.
 */
public interface JnSystemMessage {

	/**
	 * The name of the enum item.
	 * @return the name
	 */
	String name();

	/**
	 * Name under which the text of this item is stored in {@link JnEntitySystemMessage}: the name of the enum followed by
	 * the name of the item.
	 * @return the name of the system message
	 */
	default String getSystemMessageName() {
		Enum<?> item = (Enum<?>) this;
		Class<?> declaringClass = item.getDeclaringClass();
		String className = declaringClass.getName();
		String systemMessageName = className + "." + this.name();
		return systemMessageName;
	}

	/**
	 * Primary key of the {@link JnEntitySystemMessage} record that holds the text of this item in the given language.
	 * @param language the language
	 * @return the primary key
	 */
	default CcpJsonRepresentation getSystemMessageId(JnLanguage language) {
		String systemMessageName = this.getSystemMessageName();

		CcpJsonRepresentation jsonWithSystemMessageName = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySystemMessage.Fields.systemMessageName, systemMessageName);

		CcpJsonRepresentation systemMessageId = jsonWithSystemMessageName
				.put(JnJsonCommonsFields.language, language);

		return systemMessageId;
	}

	/**
	 * The text of this item in the given language. Languages without their own record get the English one.
	 * @param language the language
	 * @return the text
	 */
	default String getMessage(JnLanguage language) {
		CcpJsonRepresentation systemMessageId = this.getSystemMessageId(language);
		CcpJsonRepresentation englishSystemMessageId = this.getSystemMessageId(JnLanguage.english);

		CcpBusiness readTheEnglishMessage = notFoundContext -> JnEntitySystemMessage.ENTITY.getOneById(englishSystemMessageId);

		CcpEntityMetaData entityMetaData = JnEntitySystemMessage.ENTITY.getEntityMetaData();
		CcpJsonRepresentation systemMessage = entityMetaData.getOneByIdOrHandleItIfThisIdWasNotFound(systemMessageId, readTheEnglishMessage);

		List<String> messageLines = systemMessage.getAsStringList(JnJsonCommonsFields.message);
		String message = messageLines.get(0);
		return message;
	}

	/**
	 * The text of this item in the given language with its {@code {field}} placeholders replaced by the values of the
	 * parameters.
	 * @param language the language
	 * @param parameters the values of the placeholders
	 * @return the resolved text
	 */
	default String getMessage(JnLanguage language, CcpJsonRepresentation parameters) {
		String template = this.getMessage(language);
		CcpStringDecorator templateDecorator = new CcpStringDecorator(template);
		CcpTextDecorator templateText = templateDecorator.text();
		CcpTextDecorator resolvedTemplate = templateText.resolveTemplate(parameters);
		String message = resolvedTemplate.content;
		return message;
	}

	/**
	 * Initial load of the text of this item in the given language, to be returned by a {@code getFirstRecordsToInsert}.
	 * @param language the language
	 * @param text the text
	 * @return the bulk items of the record
	 */
	default List<CcpBulkItem> toBulkItems(JnLanguage language, String text) {
		CcpJsonRepresentation systemMessageId = this.getSystemMessageId(language);
		List<String> messageLines = Arrays.asList(text);
		CcpJsonRepresentation systemMessage = systemMessageId.put(JnJsonCommonsFields.message, messageLines);
		List<CcpBulkItem> bulkItems = JnEntitySystemMessage.ENTITY.toBulkItems(systemMessage, CcpBulkEntityOperationType.create);
		return bulkItems;
	}
}
