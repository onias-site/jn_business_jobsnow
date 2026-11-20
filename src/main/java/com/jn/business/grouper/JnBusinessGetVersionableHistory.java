package com.jn.business.grouper;

import java.util.Collection;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.jn.utils.JnVersionableHistory;

/**
 * The history of a record of a versionable entity, read by id ({@code entity} and {@code id}, the document id of the
 * record) from its group of {@code jn_grouper} ({@link JnVersionableHistory}), for the logged-in user: only when a value of
 * the primary key of the record is the e-mail of the session, as it came or as the entities keep it (its SHA-1). The
 * history keeps the whole record before each write (the hash of a password, the answers of the login, the data of a
 * recruiter), so the history of another person's record answers 404, the same answer of a record with no history, and
 * the endpoint does not tell which records exist (the same rule of the groups, since 2026-11-10).
 * <p>
 * Output: {@code entity}, {@code id} and {@code versions}, oldest first, each with {@code operation}, {@code date},
 * {@code timestamp} and {@code json} (the record before the write).
 */
public class JnBusinessGetVersionableHistory implements CcpBusiness {

	/** The input of this business, with its validation rules. */
	public static enum Fields implements CcpJsonFieldName {
		/** The e-mail of the session. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		email,

		/** The name of the entity of the record. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		entity,

		/** The document id of the record. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		id,
	}

	/** The single instance. */
	public static final JnBusinessGetVersionableHistory INSTANCE = new JnBusinessGetVersionableHistory();

	private JnBusinessGetVersionableHistory() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Reads the history of the record.
	 * @param json the e-mail of the session, the name of the entity and the id of the record
	 * @return the history
	 * @throws CcpErrorFlowDisturb with {@code NOT_FOUND} when the record has no history or is not of the logged-in user
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String entityName = json.getAsString(Fields.entity);
		String recordId = json.getAsString(Fields.id);
		String parentName = JnVersionableHistory.getParentName();
		String parentId = JnVersionableHistory.getParentId(entityName, recordId);
		CcpJsonRepresentation group = JnGroupers.getGroup(false, parentName, entityName, parentId);
		boolean noHistory = group.isEmpty();

		if(noHistory) {
			throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
		}

		boolean anotherOwner = false == this.isOfTheSessionOwner(json, group);

		if(anotherOwner) {
			throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
		}

		List<CcpJsonRepresentation> versions = JnVersionableHistory.getVersions(group);
		CcpJsonRepresentation historyWithEntity = CcpOtherConstants.EMPTY_JSON.put(Fields.entity, entityName);
		CcpJsonRepresentation historyWithId = historyWithEntity.put(Fields.id, recordId);
		CcpJsonRepresentation history = historyWithId.put(JnVersionableHistory.Fields.versions, versions);
		return history;
	}

	/**
	 * Tells whether a value of the primary key of the record is the e-mail of the session, as it came or hashed.
	 * @param json the input, with the e-mail of the session
	 * @param group the group of the history
	 * @return {@code true} for a record of the logged-in user
	 */
	private boolean isOfTheSessionOwner(CcpJsonRepresentation json, CcpJsonRepresentation group) {
		String email = json.getAsString(JnEntityLoginEmail.Fields.email);
		CcpStringDecorator emailDecorator = new CcpStringDecorator(email);
		CcpHashDecorator emailHashDecorator = emailDecorator.hash();
		String emailHash = emailHashDecorator.asString(CcpHashAlgorithm.SHA1);
		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		CcpJsonRepresentation primaryKey = parentJson.getInnerJson(JnVersionableHistory.Fields.primaryKey);
		Collection<Object> primaryKeyValues = primaryKey.content.values();
		for (Object primaryKeyValue : primaryKeyValues) {
			String primaryKeyText = "" + primaryKeyValue;
			boolean theEmail = primaryKeyText.equals(email) || primaryKeyText.equals(emailHash);

			if(theEmail) {
				return true;
			}
		}
		return false;
	}
}
