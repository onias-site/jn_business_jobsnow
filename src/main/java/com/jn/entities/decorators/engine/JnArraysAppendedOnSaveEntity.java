package com.jn.entities.decorators.engine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldAppendedOnSave;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Decorator of {@code @CcpEntityArraysAppendedOnSave}: the write of the record becomes an atomic update run by the
 * database ({@code scriptedUpdate}), which adds the items of the lists marked with {@code @CcpEntityFieldAppendedOnSave}
 * to the stored ones (repeated items included) and replaces the other fields; a record that does not exist yet is
 * created by the same script. Two saves of the same record at the same moment are applied one after the other, so both
 * get their items in; a plain save would send the whole list, and the second one would lose the item of the first.
 * <p>
 * It is the innermost decorator (priority 0): it changes only the item of the entity itself, which the decorators
 * around it still adjust ({@code Disposable} gives it the id of the period). In a disposable entity, the copy kept in
 * {@code jn_disposable_record} has only the items of the last save; the record of the period, which the reads find
 * first, has them all.
 */
public class JnArraysAppendedOnSaveEntity extends CcpDefaultEntityDelegator<Object> {

	/** The script run over the record, and its parameters. */
	enum Scripts implements CcpJsonFieldName {
		/** Replaces the fields of {@code set} and adds the items of each list of {@code append} to the stored list. */
		appendToArrays {
			public String getValue() {
				return "for (def field : params.set.entrySet()) { ctx._source[field.getKey()] = field.getValue(); } "
						+ "for (def field : params.append.entrySet()) { "
						+ "if (ctx._source[field.getKey()] == null) { ctx._source[field.getKey()] = new ArrayList(); } "
						+ "ctx._source[field.getKey()].addAll(field.getValue()); }";
			}
		},
		/** Parameter: the fields replaced as in any save. */
		set,
		/** Parameter: the lists whose items are added to the stored ones. */
		append,
	}

	/**
	 * Wraps the entity. The fields are read from the metadata only when a record is written: while the chain is being
	 * built, the metadata does not exist yet.
	 * @param entity the entity decorated so far
	 */
	public JnArraysAppendedOnSaveEntity(CcpEntity entity) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
	}

	/**
	 * The names of the fields that are not appended, replaced on each save.
	 * @param metaData the metadata of the entity
	 * @param appendedFields the names of the appended fields
	 * @return the names of the other fields
	 */
	private static List<String> getReplacedFields(CcpEntityMetaData metaData, List<String> appendedFields) {
		List<String> replacedFields = new ArrayList<>();
		for (CcpEntityField field : metaData.allFields) {
			boolean appended = appendedFields.contains(field.name);

			if(appended) {
				continue;
			}

			replacedFields.add(field.name);
		}
		return replacedFields;
	}

	/**
	 * The names of the fields marked with {@code @CcpEntityFieldAppendedOnSave}.
	 * @param configurationClass the configurator class of the entity
	 * @return the names of the fields
	 * @throws JnErrorNoFieldIsAppendedOnSave when there is none
	 */
	private static List<String> getAppendedFields(Class<?> configurationClass) {
		Class<?> fieldsClass = CcpEntityFactory.getFieldsClass(configurationClass);
		Field[] declaredFields = fieldsClass.getDeclaredFields();
		List<String> appendedFields = new ArrayList<>();
		for (Field declaredField : declaredFields) {
			boolean notMarked = false == declaredField.isAnnotationPresent(CcpEntityFieldAppendedOnSave.class);

			if(notMarked) {
				continue;
			}

			String fieldName = declaredField.getName();
			appendedFields.add(fieldName);
		}

		boolean noFieldMarked = appendedFields.isEmpty();

		if(noFieldMarked) {
			throw new JnErrorNoFieldIsAppendedOnSave(configurationClass);
		}

		return appendedFields;
	}

	/**
	 * The items of the wrapped entity, with the create or update of this entity turned into the atomic update that adds
	 * the items of the lists; deletes and the items of other entities are kept.
	 * @param json the record
	 * @param operation the bulk operation
	 * @return the bulk items
	 */
	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		boolean notAWrite = CcpBulkEntityOperationType.create != operation && CcpBulkEntityOperationType.update != operation;

		if(notAWrite) {
			return bulkItems;
		}

		List<CcpBulkItem> items = new ArrayList<>();
		for (CcpBulkItem bulkItem : bulkItems) {
			CcpBulkItem item = this.toAppendingItem(bulkItem);
			items.add(item);
		}
		return items;
	}

	/**
	 * Turns the write of this entity into the atomic update. The fields of the record stay in the json of the item, next
	 * to the ones of the script: the save searches the record by them, and the database reads only the script.
	 * @param bulkItem a write item
	 * @return the atomic update, or the item itself when it belongs to another entity
	 */
	private CcpBulkItem toAppendingItem(CcpBulkItem bulkItem) {
		CcpEntityMetaData metaData = this.getEntityMetaData();
		CcpEntityMetaData itemMetaData = bulkItem.entity.getEntityMetaData();
		boolean anotherEntity = false == itemMetaData.entityName.equals(metaData.entityName);

		if(anotherEntity) {
			return bulkItem;
		}

		List<String> appendedFields = getAppendedFields(metaData.configurationClass);
		List<String> replacedFields = getReplacedFields(metaData, appendedFields);
		CcpJsonRepresentation replaced = bulkItem.json.getJsonPiece(replacedFields);
		CcpJsonRepresentation appended = bulkItem.json.getJsonPiece(appendedFields);
		CcpJsonRepresentation paramsWithReplaced = CcpOtherConstants.EMPTY_JSON.put(Scripts.set, replaced);
		CcpJsonRepresentation params = paramsWithReplaced.put(Scripts.append, appended);
		String source = Scripts.appendToArrays.getValue();
		CcpJsonRepresentation itemWithScript = bulkItem.json.put(CcpBulkScriptedUpdateFields.script, source);
		CcpJsonRepresentation itemWithParams = itemWithScript.put(CcpBulkScriptedUpdateFields.params, params);
		CcpJsonRepresentation itemWithUpsert = itemWithParams.put(CcpBulkScriptedUpdateFields.upsert, CcpOtherConstants.EMPTY_JSON);
		// the script also runs over the empty upsert: the record that does not exist yet is created by it
		CcpJsonRepresentation item = itemWithUpsert.put(CcpBulkScriptedUpdateFields.scriptedUpsert, true);
		CcpBulkItem appendingItem = new CcpBulkItem(item, CcpBulkEntityOperationType.scriptedUpdate, bulkItem.entity, bulkItem.id);
		return appendingItem;
	}

	/** Raised when an entity annotated with {@code @CcpEntityArraysAppendedOnSave} marks no field to append. */
	@SuppressWarnings("serial")
	private static class JnErrorNoFieldIsAppendedOnSave extends RuntimeException {

		/**
		 * Builds the error.
		 * @param configurationClass the configurator class of the entity
		 */
		private JnErrorNoFieldIsAppendedOnSave(Class<?> configurationClass) {
			super(configurationClass.getName() + " is annotated with @CcpEntityArraysAppendedOnSave, but none of its fields is marked with @CcpEntityFieldAppendedOnSave");
		}
	}
}
