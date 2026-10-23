package com.jn.business.grouper;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * The read of a group of {@code JnEntityGrouper} on both sides at once, by id (its primary key: {@code parentName},
 * {@code childName} and {@code parentId}), in a single search ({@code getOneByIdAnyWhere}): the same group can exist on
 * both sides, some children active and others inactive.
 * <p>
 * Output: one key per side, named after its entity ({@code jn_grouper}, the active records, and
 * {@code jn_inactive_grouper}, the inactive ones), each one with the group on that side, or an empty json when it does
 * not exist there.
 */
public class JnBusinessGetGroupOnBothSides implements CcpBusiness {

	/** The input of this business, with its validation rules. */
	public static enum Fields implements CcpJsonFieldName {
		/** The kind of group: the name of the entity that calculates its parent id. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		parentName,

		/** The name of the entity of the children. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		childName,

		/** The id of the parent. */
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		@CcpJsonFieldValidatorRequired
		parentId,
	}

	/** The single instance. */
	public static final JnBusinessGetGroupOnBothSides INSTANCE = new JnBusinessGetGroupOnBothSides();

	private JnBusinessGetGroupOnBothSides() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Reads the group on both sides.
	 * @param json the primary key of the group
	 * @return the group of each side under the name of its entity, an empty json on a side where it does not exist
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String parentName = json.getAsString(Fields.parentName);
		String childName = json.getAsString(Fields.childName);
		String parentId = json.getAsString(Fields.parentId);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentName, childName, parentId);
		CcpJsonRepresentation groupOnBothSides = JnEntityGrouper.ENTITY.getOneByIdAnyWhere(groupKey);
		return groupOnBothSides;
	}
}
