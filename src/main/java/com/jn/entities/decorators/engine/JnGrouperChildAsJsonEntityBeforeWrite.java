package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * Decorator of {@code @CcpEntityGrouperChild} (given as its {@code beforeDecoratorClass}) that keeps in the groups of
 * {@code JnEntityGrouper} the record itself, as a JSON text, instead of its id: a history the front end parses with no
 * read of the grouped entity. It stays in the outer part of the chain (priority 7), before the field transformers, so the
 * child keeps the values as they came (the field transformers still store them transformed in the entity).
 * <ul>
 * <li>save: fills {@code date} with the current date and time ({@code dd/MM/yyyy HH:mm:ss}), saves and then puts, in the
 * groups of its parents, the record without the fields its parents come from and with that date, in place of the earlier
 * version of the same record (the children that contain the primary key fields that do not come from the parents);</li>
 * <li>delete and deleteAnyWhere: the groups are a history and are left as they are.</li>
 * </ul>
 * The parents are calculated from the record as it came, the primary key of each one transformed as the entity of the
 * parent ids does it (the e-mail becomes its SHA-1, for one); the groups are always on the main side of the grouper. Its
 * {@code afterDecoratorClass} is {@code JnGrouperChildAsJsonEntityAfterWrite}, which does nothing.
 */
public class JnGrouperChildAsJsonEntityBeforeWrite extends JnGrouperChildEntityOnWrite {

	/** The texts of this decorator. */
	enum Constants implements CcpJsonFieldName {
		/** The format of {@code date}. */
		DATE_FORMAT {
			public String getValue() {
				return "dd/MM/yyyy HH:mm:ss";
			}
		},
	}

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildAsJsonEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity, annotation);
	}

	/**
	 * Saves the record with the date of the write and the ids of its parents, then puts it, as a JSON text, in their
	 * groups in place of its earlier version.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		String dateFormat = Constants.DATE_FORMAT.getValue();
		CcpTimeDecorator now = new CcpTimeDecorator();
		String currentDate = now.getFormattedDateTime(dateFormat);
		CcpJsonRepresentation jsonWithDate = json.put(JnJsonCommonsFields.date, currentDate);
		JnEntityGrouperParents parents = this.getParents();
		Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecord(jsonWithDate);
		CcpJsonRepresentation jsonWithParentIds = parents.putParentIds(jsonWithDate, recordParents);
		boolean inserted = this.entity.save(jsonWithParentIds);

		Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
		Set<String> parentFields = getParentFields(parentJsons);
		CcpJsonRepresentation childRecord = this.getChildRecord(jsonWithDate, parents, parentFields);
		String child = childRecord.asUgglyJson();
		List<String> childKeys = this.getChildKeys(jsonWithDate, parentFields);
		String parentName = parents.getParentName();
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpEntity grouper = JnGroupers.getGrouper(false);
		JnGroupers.replaceChild(grouper, parentName, entityDetails.entityName, childKeys, child, recordParents);
		return inserted;
	}

	/**
	 * The fields the parents come from.
	 * @param parentJsons the values of each parent
	 * @return the names of their fields
	 */
	private static Set<String> getParentFields(Collection<CcpJsonRepresentation> parentJsons) {
		Set<String> parentFields = new HashSet<>();
		for (CcpJsonRepresentation parentJson : parentJsons) {
			Set<String> fieldsOfTheParent = parentJson.fieldSet();
			parentFields.addAll(fieldsOfTheParent);
		}
		return parentFields;
	}

	/**
	 * The record kept in the groups: the fields of the entity present in it, but the ones its parents come from (they are
	 * in the group already) and the list of the parent ids.
	 * @param record the record, with its date
	 * @param parents the parents configured for the entity
	 * @param parentFields the fields the parents come from
	 * @return the child record
	 */
	private CcpJsonRepresentation getChildRecord(CcpJsonRepresentation record, JnEntityGrouperParents parents, Set<String> parentFields) {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		List<String> childFields = new ArrayList<>();
		for (CcpEntityField field : entityDetails.allFields) {
			String fieldName = field.name();
			boolean fromTheParents = parentFields.contains(fieldName);

			if(fromTheParents) {
				continue;
			}

			childFields.add(fieldName);
		}
		CcpJsonRepresentation childRecord = record.getJsonPiece(childFields);
		CcpJsonRepresentation childRecordWithoutParentIds = parents.removeParentIds(childRecord);
		return childRecordWithoutParentIds;
	}

	/**
	 * The texts that only the children of this record contain: each field of its primary key that does not come from the
	 * parents, as it is written in the JSON text of the child ({@code "name":value}).
	 * @param record the record
	 * @param parentFields the fields the parents come from
	 * @return the texts
	 */
	private List<String> getChildKeys(CcpJsonRepresentation record, Set<String> parentFields) {
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		List<String> childKeys = new ArrayList<>();
		for (String primaryKeyName : entityDetails.primaryKeyNames) {
			boolean fromTheParents = parentFields.contains(primaryKeyName);

			if(fromTheParents) {
				continue;
			}

			CcpFieldName primaryKeyField = new CcpFieldName(primaryKeyName);
			Object value = record.get(primaryKeyField);
			CcpJsonRepresentation fieldAlone = CcpOtherConstants.EMPTY_JSON.put(primaryKeyField, value);
			String fieldAloneText = fieldAlone.asUgglyJson();
			int closingBrace = fieldAloneText.length() - 1;
			String childKey = fieldAloneText.substring(1, closingBrace);
			childKeys.add(childKey);
		}
		return childKeys;
	}
}
