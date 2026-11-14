package com.jn.entities;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonCopyFieldValidationsFrom;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * Virtual entity whose id identifies, in {@code JnEntityGrouper}, the group that keeps how many records an entity
 * annotated with {@code @CcpEntityGrouperChildTotal} has now: one group per entity (its {@code entity} field is the name
 * of the main side, and so is the {@code childName} of the group), whose only child is the JSON text
 * {@code {"total": n}}. The main side of the grouper counts the main side of the entity and, for a twin entity, the
 * twin side of the grouper ({@code jn_inactive_grouper}) counts its twin side, under the same parent id and child name.
 * A total of 0 is a group that does not exist. Cached for 1 hour.
 */
@CcpEntityCache(3600)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
@CcpEntityFieldsValidator(JnEntityVirtualRecordsTotal.Fields.class)
public class JnEntityVirtualRecordsTotal {

	/** The entity {@code jn_virtual_records_total}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityVirtualRecordsTotal.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code entity} field: the primary key, the name of the main side of the counted entity. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonCopyFieldValidationsFrom(JnJsonCommonsFields.class)
		entity,
		;
	}

	/** The only key of the JSON text that is the child of the group. */
	public static enum Counters implements CcpJsonFieldName{
		/** How many records the side of the entity has now. */
		total,
	}

	/**
	 * The values the parent id of the group of the entity comes from: {@code {"entity": <entityName>}}.
	 * @param entityName the name of the main side of the counted entity
	 * @return the parent values
	 */
	public static CcpJsonRepresentation getParentJson(String entityName) {
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(Fields.entity, entityName);
		return parentJson;
	}

	/**
	 * The parent id of the group of the entity, the {@code parentId} of its group in {@code JnEntityGrouper}.
	 * @param entityName the name of the main side of the counted entity
	 * @return the parent id
	 */
	public static String getParentId(String entityName) {
		CcpJsonRepresentation parentJson = getParentJson(entityName);
		String parentId = ENTITY.calculateId(parentJson);
		return parentId;
	}

	/**
	 * How many records one side of the entity has now, read by id from its group.
	 * @param twinSide whether the twin side of the entity (kept in the twin side of the grouper) is the one read
	 * @param entityName the name of the main side of the counted entity
	 * @return the total, 0 when the group does not exist
	 */
	public static long getTotal(boolean twinSide, String entityName) {
		CcpEntityMetaData parentDetails = ENTITY.getEntityMetaData();
		String parentId = getParentId(entityName);
		CcpJsonRepresentation group = JnGroupers.getGroup(twinSide, parentDetails.entityName, entityName, parentId);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		boolean noGroup = children.isEmpty();

		if(noGroup) {
			return 0L;
		}

		String countersText = children.get(0);
		CcpJsonRepresentation counters = new CcpJsonRepresentation(countersText);
		boolean noTotal = false == counters.containsAllFields(Counters.total);

		if(noTotal) {
			return 0L;
		}

		Long total = counters.getAsLongNumber(Counters.total);
		return total;
	}
}
