package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * <li>delete and deleteAnyWhere of an entity without twin: the groups are a history and are left as they are;</li>
 * <li>in a twin entity (since 2026-11-15) the groups follow the sides of the entity: the delete moves the child from the
 * groups of the side it leaves to the groups of the side it goes to (the main records in {@code jn_grouper}, the twin ones
 * in {@code jn_inactive_grouper}), the save takes it out of the groups of the other side, and the deleteAnyWhere takes it
 * out of the groups of both sides. A child moved keeps its {@code date} and stays in the groups of the period of that
 * date (the month of the view, for a disposable parent), with the fields the delete brought (a comment, for one).</li>
 * </ul>
 * The parents are calculated from the record as it came, the primary key of each one transformed as the entity of the
 * parent ids does it (the e-mail becomes its SHA-1, for one), so the delete, like the save, expects the primary key as it
 * came (the LinkedIn address, not its hash). Its {@code afterDecoratorClass} is
 * {@code JnGrouperChildAsJsonEntityAfterWrite}, which does nothing.
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
	 * Saves the record with the date of the write and the ids of its parents of every kind, then puts it, as a JSON text,
	 * in the groups of each kind in place of its earlier version; in a twin entity the record first leaves the groups of the
	 * other side, as it leaves that side of the entity.
	 * @param json the record, its fields not transformed yet
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		this.removeFromTheGroupsOfTheOtherSide(json);
		String dateFormat = Constants.DATE_FORMAT.getValue();
		CcpTimeDecorator now = new CcpTimeDecorator();
		String currentDate = now.getFormattedDateTime(dateFormat);
		CcpJsonRepresentation jsonWithDate = json.put(JnJsonCommonsFields.date, currentDate);
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		Map<JnEntityGrouperParents, Map<String, CcpJsonRepresentation>> recordParentsByKind = new LinkedHashMap<>();
		CcpJsonRepresentation jsonWithParentIds = jsonWithDate;
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecord(jsonWithDate);
			recordParentsByKind.put(parents, recordParents);
			jsonWithParentIds = parents.putParentIds(jsonWithParentIds, recordParents);
		}
		boolean inserted = this.entity.save(jsonWithParentIds);

		String mainEntityName = this.getMainEntityName();
		boolean twinSide = this.isTwinSide();
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = recordParentsByKind.get(parents);
			Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
			Set<String> parentFields = getParentFields(parentJsons);
			CcpJsonRepresentation childRecord = this.getChildRecord(jsonWithDate, allParents, parentFields);
			String child = childRecord.asUgglyJson();
			List<String> childKeys = this.getChildKeys(jsonWithDate, parentFields);
			String parentName = parents.getParentName();
			JnGroupers.replaceChild(grouper, parentName, mainEntityName, childKeys, child, recordParents);
		}
		return inserted;
	}

	/**
	 * Deletes; in a twin entity the record changes sides (the deactivation on the main side, the reactivation on the twin
	 * side), and its child goes, with it, from the groups of the side it leaves to the groups of the side it goes to.
	 * @param json the record (at least its primary key, as it came)
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		boolean noSides = false == this.hasTwin();

		if(noSides) {
			return this.entity.delete(json);
		}

		boolean fromTwinSide = this.isTwinSide();
		CcpEntity sideLeft = this.getSideEntity(fromTwinSide);
		boolean notStored = false == sideLeft.exists(json);

		if(notStored) {
			return this.entity.delete(json);
		}

		CcpJsonRepresentation storedRecord = sideLeft.getOneById(json);
		boolean deleted = this.entity.delete(json);
		this.moveBetweenTheGroups(fromTwinSide, json, storedRecord);
		return deleted;
	}

	/**
	 * Moves the child of the record from the groups of the side it left to the groups of the side it went to, in the period
	 * of its {@code date}: the child as the groups of the side left kept it, with the fields the delete brought (a comment,
	 * for one); when those groups do not have it, the child is built from the stored record.
	 * @param fromTwinSide whether the record left the twin side
	 * @param json the record, as the delete brought it
	 * @param storedRecord the record as it was stored on the side left
	 */
	private void moveBetweenTheGroups(boolean fromTwinSide, CcpJsonRepresentation json, CcpJsonRepresentation storedRecord) {
		Long momentOfTheWrite = getMomentOfTheWrite(storedRecord);
		boolean toTwinSide = false == fromTwinSide;
		String mainEntityName = this.getMainEntityName();
		CcpEntity grouperLeft = JnGroupers.getGrouper(fromTwinSide);
		CcpEntity grouperReached = JnGroupers.getGrouper(toTwinSide);
		CcpJsonRepresentation movedRecord = storedRecord.mergeWithAnotherJson(json);
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecordAtTheMoment(json, momentOfTheWrite);
			Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
			Set<String> parentFields = getParentFields(parentJsons);
			List<String> childKeys = this.getChildKeys(json, parentFields);
			String parentName = parents.getParentName();
			Set<String> parentIdsSet = recordParents.keySet();
			List<String> parentIds = new ArrayList<>(parentIdsSet);
			CcpJsonRepresentation fieldsOfTheDelete = this.getChildRecord(json, allParents, parentFields);
			CcpJsonRepresentation childBuiltFromTheRecord = this.getChildRecord(movedRecord, allParents, parentFields);
			CcpJsonRepresentation keptChild = findTheKeptChild(fromTwinSide, parentName, mainEntityName, parentIds, childKeys, childBuiltFromTheRecord);
			CcpJsonRepresentation movedChild = keptChild.mergeWithAnotherJson(fieldsOfTheDelete);
			String child = movedChild.asUgglyJson();
			JnGroupers.removeChildByKeys(grouperLeft, parentName, mainEntityName, childKeys, parentIds);
			JnGroupers.replaceChild(grouperReached, parentName, mainEntityName, childKeys, child, recordParents);
		}
	}

	/**
	 * The child of the record as the groups of a side keep it: the first child, among the groups of the parents, that
	 * contains every text of {@code childKeys}.
	 * @param twinSide whether the twin side of the grouper is the one read
	 * @param parentName the kind of group
	 * @param childName the name of the main entity
	 * @param parentIds the parent ids
	 * @param childKeys the texts that only the children of the record contain
	 * @param childWhenNotKept the child returned when no group keeps it
	 * @return the child, parsed
	 */
	private static CcpJsonRepresentation findTheKeptChild(boolean twinSide, String parentName, String childName, List<String> parentIds, List<String> childKeys, CcpJsonRepresentation childWhenNotKept) {
		for (String parentId : parentIds) {
			List<String> children = JnGroupers.getChildren(twinSide, parentName, childName, parentId);
			for (String child : children) {
				boolean anotherRecord = false == containsEveryKey(child, childKeys);

				if(anotherRecord) {
					continue;
				}

				return new CcpJsonRepresentation(child);
			}
		}
		return childWhenNotKept;
	}

	/**
	 * Whether a child contains every text of {@code childKeys}.
	 * @param child the child, a JSON text
	 * @param childKeys the texts
	 * @return {@code true} when every text is in the child
	 */
	private static boolean containsEveryKey(String child, List<String> childKeys) {
		for (String childKey : childKeys) {
			boolean missing = false == child.contains(childKey);

			if(missing) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Deletes from both sides; in a twin entity the child leaves the groups of the sides where the record is stored.
	 * @param json the record (at least its primary key, as it came)
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		boolean noSides = false == this.hasTwin();

		if(noSides) {
			return this.entity.deleteAnyWhere(json);
		}

		this.removeFromTheGroupsOfTheSide(false, json);
		this.removeFromTheGroupsOfTheSide(true, json);
		boolean deleted = this.entity.deleteAnyWhere(json);
		return deleted;
	}

	/**
	 * In a twin entity, takes the child of the record out of the groups of the side opposite to this one, when the record
	 * is stored there (the save writes on its side and deletes from the other one).
	 * @param json the record, as it came
	 */
	private void removeFromTheGroupsOfTheOtherSide(CcpJsonRepresentation json) {
		boolean noSides = false == this.hasTwin();

		if(noSides) {
			return;
		}

		boolean twinSide = this.isTwinSide();
		boolean otherSide = false == twinSide;
		this.removeFromTheGroupsOfTheSide(otherSide, json);
	}

	/**
	 * Takes the child of the record out of the groups of a side, when the record is stored on that side of the entity: the
	 * groups of the period of its {@code date}.
	 * @param twinSide whether the twin side is the one checked
	 * @param json the record, as it came
	 */
	private void removeFromTheGroupsOfTheSide(boolean twinSide, CcpJsonRepresentation json) {
		CcpEntity sideEntity = this.getSideEntity(twinSide);
		boolean notStored = false == sideEntity.exists(json);

		if(notStored) {
			return;
		}

		CcpJsonRepresentation storedRecord = sideEntity.getOneById(json);
		Long momentOfTheWrite = getMomentOfTheWrite(storedRecord);
		this.removeFromTheGroups(twinSide, json, momentOfTheWrite);
	}

	/**
	 * Takes the child of the record out of the groups of every kind, on a side of the grouper, in the period of a moment.
	 * @param twinSide whether the twin side of the grouper is the one written
	 * @param json the record, as it came
	 * @param momentOfTheWrite the moment of the last save of the record
	 */
	private void removeFromTheGroups(boolean twinSide, CcpJsonRepresentation json, Long momentOfTheWrite) {
		String mainEntityName = this.getMainEntityName();
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecordAtTheMoment(json, momentOfTheWrite);
			Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
			Set<String> parentFields = getParentFields(parentJsons);
			List<String> childKeys = this.getChildKeys(json, parentFields);
			Set<String> parentIdsSet = recordParents.keySet();
			List<String> parentIds = new ArrayList<>(parentIdsSet);
			String parentName = parents.getParentName();
			JnGroupers.removeChildByKeys(grouper, parentName, mainEntityName, childKeys, parentIds);
		}
	}

	/**
	 * The moment of the last save of a stored record, read from its {@code date}; the current moment when it has none.
	 * @param storedRecord the record as it is stored
	 * @return the moment, in milliseconds
	 */
	private static Long getMomentOfTheWrite(CcpJsonRepresentation storedRecord) {
		boolean noDate = false == storedRecord.containsAllFields(JnJsonCommonsFields.date);

		if(noDate) {
			CcpTimeDecorator now = new CcpTimeDecorator();
			return now.getContent();
		}

		String date = storedRecord.getAsString(JnJsonCommonsFields.date);
		String dateFormat = Constants.DATE_FORMAT.getValue();
		CcpTimeDecorator momentOfTheWrite = new CcpTimeDecorator(date, dateFormat);
		return momentOfTheWrite.getContent();
	}

	/**
	 * The name of the main entity: the child name of the groups on both sides.
	 * @return the entity name
	 */
	private String getMainEntityName() {
		CcpEntity mainEntity = this.getSideEntity(false);
		CcpEntityMetaData mainDetails = mainEntity.getEntityMetaData();
		return mainDetails.entityName;
	}

	/**
	 * The fields the parents come from.
	 * @param parentJsons the values of each parent
	 * @return the names of their fields
	 */
	protected static Set<String> getParentFields(Collection<CcpJsonRepresentation> parentJsons) {
		Set<String> parentFields = new HashSet<>();
		for (CcpJsonRepresentation parentJson : parentJsons) {
			Set<String> fieldsOfTheParent = parentJson.fieldSet();
			parentFields.addAll(fieldsOfTheParent);
		}
		return parentFields;
	}

	/**
	 * The record kept in the groups of one kind: the fields of the entity present in it, but the ones its parents of that
	 * kind come from (they are in the group already) and the lists of the parent ids of every kind.
	 * @param record the record, with its date
	 * @param allParents the parents configured for the entity, one per kind of group
	 * @param parentFields the fields the parents of the kind come from
	 * @return the child record
	 */
	protected CcpJsonRepresentation getChildRecord(CcpJsonRepresentation record, List<JnEntityGrouperParents> allParents, Set<String> parentFields) {
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
		CcpJsonRepresentation childRecordWithoutParentIds = record.getJsonPiece(childFields);
		for (JnEntityGrouperParents parents : allParents) {
			childRecordWithoutParentIds = parents.removeParentIds(childRecordWithoutParentIds);
		}
		return childRecordWithoutParentIds;
	}

	/**
	 * The texts that only the children of this record contain: each field of its primary key that does not come from the
	 * parents, as it is written in the JSON text of the child ({@code "name":value}).
	 * @param record the record
	 * @param parentFields the fields the parents come from
	 * @return the texts
	 */
	protected List<String> getChildKeys(CcpJsonRepresentation record, Set<String> parentFields) {
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
