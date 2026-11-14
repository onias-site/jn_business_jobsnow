package com.jn.utils;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;

/**
 * The description of the group of {@code jn_grouper} behind a chart, as the providers of the charts of a screen
 * ({@code JnEntityGrouperChartsProviders}) return them to {@code JnBusinessGetGrouperCharts}: the chart it feeds, the
 * primary key of the group, the side of the grouper and, optionally, the key under which the sum of the counters of the
 * group goes (two groups of the same kind on different sides, the active and the inactive records, become two slices).
 */
public class JnGrouperChart {

	/** The fields of a description and of the output of a provider. */
	public static enum Fields implements CcpJsonFieldName {
		/** The output of a provider: the list of descriptions. */
		charts,
		/** The name of the chart the group feeds; the counters of every group of the same chart are merged. */
		chart,
		/** The kind of group. */
		parentName,
		/** The name of the entity of the children. */
		childName,
		/** The id of the parent. */
		parentId,
		/** The side of the grouper ({@code mainEntity} or {@code twinEntity}). */
		side,
		/** When present, the key under which the sum of the counters of the group goes, in place of its own keys. */
		sumAs,
	}

	private JnGrouperChart() {}

	/**
	 * A chart whose slices are the counters of a group on the main side, as they are.
	 * @param chart the name of the chart
	 * @param parentEntity the entity whose id is the parent id of the group (a virtual counter)
	 * @param childEntity the entity counted
	 * @param combination the values the parent id is calculated from
	 * @return the description
	 */
	public static CcpJsonRepresentation counters(CcpJsonFieldName chart, CcpEntity parentEntity, CcpEntity childEntity, CcpJsonRepresentation combination) {
		CcpEntityMetaData parentDetails = parentEntity.getEntityMetaData();
		CcpEntityMetaData childDetails = childEntity.getEntityMetaData();
		String parentId = parentEntity.calculateId(combination);
		CcpJsonRepresentation description = describe(chart, parentDetails.entityName, childDetails.entityName, parentId, CcpEntityPhase.mainEntity);
		return description;
	}

	/**
	 * A slice made of the sum of the counters of a group on one side.
	 * @param chart the name of the chart
	 * @param sumAs the key of the slice
	 * @param parentName the kind of group
	 * @param childName the name of the entity of the children
	 * @param parentId the id of the parent
	 * @param side the side of the grouper
	 * @return the description
	 */
	public static CcpJsonRepresentation sum(CcpJsonFieldName chart, CcpJsonFieldName sumAs, String parentName, String childName, String parentId, CcpEntityPhase side) {
		CcpJsonRepresentation description = describe(chart, parentName, childName, parentId, side);
		String sumAsName = sumAs.getValue();
		CcpJsonRepresentation descriptionWithSum = description.put(Fields.sumAs, sumAsName);
		return descriptionWithSum;
	}

	/**
	 * The output of a provider.
	 * @param descriptions the descriptions of the groups of its charts
	 * @return {@code {"charts": [...]}}
	 */
	public static CcpJsonRepresentation output(List<CcpJsonRepresentation> descriptions) {
		CcpJsonRepresentation output = CcpOtherConstants.EMPTY_JSON.put(Fields.charts, descriptions);
		return output;
	}

	private static CcpJsonRepresentation describe(CcpJsonFieldName chart, String parentName, String childName, String parentId, CcpEntityPhase side) {
		String chartName = chart.getValue();
		String sideName = side.name();
		CcpJsonRepresentation description = CcpOtherConstants.EMPTY_JSON
				.put(Fields.chart, chartName)
				.put(Fields.parentName, parentName)
				.put(Fields.childName, childName)
				.put(Fields.parentId, parentId)
				.put(Fields.side, sideName);
		return description;
	}
}
