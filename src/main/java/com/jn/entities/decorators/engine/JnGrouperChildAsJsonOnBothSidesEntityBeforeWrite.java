package com.jn.entities.decorators.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * Decorator of {@code @CcpEntityGrouperChild} (given as its {@code beforeDecoratorClass}) that keeps the record itself,
 * as a JSON text, in the groups of {@code JnEntityGrouper}, as {@link JnGrouperChildAsJsonEntityBeforeWrite} does, but on
 * the side that matches the state of the record: an open record (a ticket of the user waiting for the support) in the
 * main groups ({@code jn_grouper}), a record already solved in the twin groups ({@code jn_inactive_grouper}). Made for the
 * tickets of the users (the "contact us" of js, the skill suggestion and the skill hierarchy fix of vis), so a screen
 * reads, by id, the open and the solved tickets of a user.
 * <ul>
 * <li>save: as {@link JnGrouperChildAsJsonEntityBeforeWrite}: the record, with the {@code date} of the write, takes the
 * place of its earlier version in the main groups; the solved versions stay in the twin groups, a history;</li>
 * <li>delete of a twin entity (the record moves to the twin, the ticket was solved) and transferDataTo (the record
 * moves to another entity, the ticket was decided): the record leaves the main groups and enters the twin groups, with
 * the values the solution brought (the answer of the support, the decision...), the {@code date} of the solution and,
 * in {@code status}, the name of the entity where the record went;</li>
 * <li>delete of an entity without twin (the user gave up the ticket): the record leaves the main groups;</li>
 * <li>deleteAnyWhere (the user gave up the ticket, or was ignored by the support): the record leaves the groups of both
 * sides, as if it had never existed.</li>
 * </ul>
 * Like {@link JnGrouperChildAsJsonEntityBeforeWrite}, it stays before the field transformers (priority 7), so the
 * parents are calculated from the record as it came. Its {@code afterDecoratorClass} is
 * {@code JnGrouperChildAsJsonEntityAfterWrite}, which does nothing.
 */
public class JnGrouperChildAsJsonOnBothSidesEntityBeforeWrite extends JnGrouperChildAsJsonEntityBeforeWrite {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildAsJsonOnBothSidesEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity, annotation);
	}

	/**
	 * Deletes and, when the record moved to the twin side of the entity, moves it to the twin groups with the values of
	 * the deletion. In an entity without twin the record is gone (the user gave up the ticket), and it leaves the main
	 * groups.
	 * @param json the record, with the values the solution brought, its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		boolean noTwin = false == this.hasTwin();

		if(noTwin) {
			this.removeFromTheGroups(false, json);
			boolean deleted = this.entity.delete(json);
			return deleted;
		}

		boolean movesToTheTwin = false == this.isTwinSide();

		if(false == movesToTheTwin) {
			boolean deleted = this.entity.delete(json);
			return deleted;
		}

		boolean notStored = false == this.entity.exists(json);

		if(notStored) {
			boolean deleted = this.entity.delete(json);
			return deleted;
		}

		CcpJsonRepresentation storedRecord = this.entity.getOneById(json);
		boolean deleted = this.entity.delete(json);
		CcpEntity twinEntity = this.getSideEntity(true);
		this.moveToTheTwinGroups(storedRecord, json, twinEntity);
		return deleted;
	}

	/**
	 * Transfers and, when the record was stored, moves it to the twin groups with the values of the transfer.
	 * @param json the record, with the values the solution brought, its fields not transformed yet
	 * @param targetEntity where the record goes
	 * @return whether the record was transferred
	 */
	public boolean transferDataTo(CcpJsonRepresentation json, CcpEntity targetEntity) {
		boolean notStored = false == this.entity.exists(json);

		if(notStored) {
			boolean transferred = this.entity.transferDataTo(json, targetEntity);
			return transferred;
		}

		CcpJsonRepresentation storedRecord = this.entity.getOneById(json);
		boolean transferred = this.entity.transferDataTo(json, targetEntity);
		this.moveToTheTwinGroups(storedRecord, json, targetEntity);
		return transferred;
	}

	/**
	 * Takes the record out of the groups of both sides and deletes it everywhere.
	 * @param json the record (at least its key), its fields not transformed yet
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		this.removeFromTheGroups(false, json);
		this.removeFromTheGroups(true, json);
		boolean deleted = this.entity.deleteAnyWhere(json);
		return deleted;
	}

	/**
	 * Takes the record out of the main groups and puts it in the twin groups: the stored values, the values the solution
	 * brought, the date of the solution and the entity where the record went.
	 * @param storedRecord the record as it was stored
	 * @param json the values the solution brought, its fields not transformed yet
	 * @param destination the entity where the record went
	 */
	private void moveToTheTwinGroups(CcpJsonRepresentation storedRecord, CcpJsonRepresentation json, CcpEntity destination) {
		this.removeFromTheGroups(false, json);

		String dateFormat = Constants.DATE_FORMAT.getValue();
		CcpTimeDecorator now = new CcpTimeDecorator();
		String currentDate = now.getFormattedDateTime(dateFormat);
		CcpEntityMetaData destinationDetails = destination.getEntityMetaData();
		// the values as they came win over the stored ones, whose primary key is already transformed
		CcpJsonRepresentation solvedRecord = storedRecord.mergeWithAnotherJson(json);
		CcpJsonRepresentation solvedRecordWithDate = solvedRecord.put(JnJsonCommonsFields.date, currentDate);

		List<JnEntityGrouperParents> allParents = this.getAllParents();
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpEntity twinGrouper = JnGroupers.getGrouper(true);
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecord(solvedRecordWithDate);
			Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
			Set<String> parentFields = getParentFields(parentJsons);
			CcpJsonRepresentation childRecord = this.getChildRecord(solvedRecordWithDate, allParents, parentFields);
			CcpJsonRepresentation fieldsOfTheDestination = this.getFieldsOfTheDestination(solvedRecord, destinationDetails, parentFields);
			CcpJsonRepresentation solvedChildRecord = childRecord.mergeWithAnotherJson(fieldsOfTheDestination);
			CcpJsonRepresentation solvedChildRecordWithStatus = solvedChildRecord.put(JnJsonCommonsFields.status, destinationDetails.entityName);
			String child = solvedChildRecordWithStatus.asUgglyJson();
			List<String> childKeys = this.getChildKeys(solvedRecordWithDate, parentFields);
			String parentName = parents.getParentName();
			JnGroupers.replaceChild(twinGrouper, parentName, entityDetails.entityName, childKeys, child, recordParents);
		}
	}

	/**
	 * The values of the solved record that are fields of the entity where it went and that its parents do not come from
	 * (the justification of a decision, the decisions of each item...).
	 * @param solvedRecord the stored values plus the values the solution brought
	 * @param destinationDetails the entity where the record went
	 * @param parentFields the fields the parents come from
	 * @return those values
	 */
	private CcpJsonRepresentation getFieldsOfTheDestination(CcpJsonRepresentation solvedRecord, CcpEntityMetaData destinationDetails, Set<String> parentFields) {
		List<String> destinationFields = new ArrayList<>();
		for (CcpEntityField field : destinationDetails.allFields) {
			String fieldName = field.name();
			boolean fromTheParents = parentFields.contains(fieldName);

			if(fromTheParents) {
				continue;
			}

			destinationFields.add(fieldName);
		}
		CcpJsonRepresentation fieldsOfTheDestination = solvedRecord.getJsonPiece(destinationFields);
		return fieldsOfTheDestination;
	}

	/**
	 * Takes every version of the record out of the groups of one side.
	 * @param twinSide whether the twin groups are the ones written
	 * @param json the record (at least its key), its fields not transformed yet
	 */
	private void removeFromTheGroups(boolean twinSide, CcpJsonRepresentation json) {
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		CcpEntityMetaData entityDetails = this.getEntityMetaData();
		CcpEntity grouper = JnGroupers.getGrouper(twinSide);
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParentsFromRawRecord(json);
			Collection<CcpJsonRepresentation> parentJsons = recordParents.values();
			Set<String> parentFields = getParentFields(parentJsons);
			List<String> childKeys = this.getChildKeys(json, parentFields);
			Set<String> parentIdsSet = recordParents.keySet();
			List<String> parentIds = new ArrayList<>(parentIdsSet);
			String parentName = parents.getParentName();
			JnGroupers.removeChildByKeys(grouper, parentName, entityDetails.entityName, childKeys, parentIds);
		}
	}
}
