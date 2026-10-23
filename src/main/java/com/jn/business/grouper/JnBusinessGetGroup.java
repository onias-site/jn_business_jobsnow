package com.jn.business.grouper;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.ccp.json.defaultvalues.annotations.CcpJsonFieldDefaultValue;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The common read of a group of {@code JnEntityGrouper}, by id (its primary key: {@code parentName}, {@code childName}
 * and {@code parentId}), on the side asked ({@code side}: {@code mainEntity}, the active records, by default, or {@code twinEntity}, the inactive ones). Every
 * reader of the groups goes through it: the endpoint of the groups and the readers of each cost center, directly or by
 * {@code JnGroupers}.
 * <p>
 * The same group can exist on both sides (some children active, others inactive), and the {@code getOneById} of a twin
 * entity answers {@code REDIRECT} when the record is only on the other side: this reads both sides by id in a single
 * search ({@code JnBusinessGetGroupOnBothSides}) and keeps the side asked, so it never redirects.
 * <p>
 * Output: the group ({@code parentId}, {@code parentName}, {@code childName}, {@code parentJson}, {@code child} and
 * {@code childCount}), or an empty json when it does not exist on that side (each caller decides what absence means).
 */
public class JnBusinessGetGroup implements CcpBusiness {

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

		/** The side of the grouper read ({@code mainEntity} or {@code twinEntity}); {@code mainEntity} when not given. */
		@CcpJsonFieldTypeString(allowedValuesEnum = CcpEntityPhase.class)
		@CcpJsonFieldDefaultValue(defaultStrings = "mainEntity")
		side,
	}

	/** The single instance. */
	public static final JnBusinessGetGroup INSTANCE = new JnBusinessGetGroup();

	private JnBusinessGetGroup() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Reads the group on the side asked.
	 * @param json the primary key of the group and the side
	 * @return the group, or an empty json when it does not exist on that side
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String sideName = json.getAsString(Fields.side);
		CcpEntityPhase side = CcpEntityPhase.valueOf(sideName);
		CcpJsonRepresentation groupOnBothSides = JnBusinessGetGroupOnBothSides.INSTANCE.execute(json);
		CcpEntity grouper = side.getEntity(JnEntityGrouper.ENTITY);
		CcpJsonRepresentation group = groupOnBothSides.getInnerJson(grouper);
		return group;
	}
}
