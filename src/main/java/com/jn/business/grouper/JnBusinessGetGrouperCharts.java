package com.jn.business.grouper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.crud.CcpUnionAllExecutor;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityPhase;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.JnEntityGrouper;
import com.jn.entities.JnEntityGrouperChartsProviders;
import com.jn.json.fields.validation.JnGrouperChartsScreens;
import com.jn.utils.JnGroupers;
import com.jn.utils.JnGrouperChart;

/**
 * The charts of a screen read from the counters of {@code jn_grouper} (OQL: only reads by id). The providers of the
 * screen ({@link JnEntityGrouperChartsProviders}, one business per cost center, run by class name) describe the groups
 * of each chart ({@link JnGrouperChart}); then every group, of both sides of the grouper, is read in a single round trip.
 * The only child of each group is the JSON text of its counters.
 * <p>
 * Output: one item per chart, with {key: total}; the counters of the groups of the same chart are merged, a group with
 * {@code sumAs} adds a single key with the sum of its counters, and a group that does not exist adds nothing.
 */
public class JnBusinessGetGrouperCharts implements CcpBusiness {

	/** The input of this business, with its validation rules. */
	public static enum Fields implements CcpJsonFieldName {
		/** The screen of the charts. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = JnGrouperChartsScreens.class)
		screen,
	}

	/** The single instance. */
	public static final JnBusinessGetGrouperCharts INSTANCE = new JnBusinessGetGrouperCharts();

	private JnBusinessGetGrouperCharts() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Reads the charts of the screen.
	 * @param json the screen
	 * @return {chart: {key: total}}
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String screenName = json.getAsString(Fields.screen);
		JnGrouperChartsScreens screen = JnGrouperChartsScreens.valueOf(screenName);
		CcpJsonRepresentation providersKey = JnEntityGrouperChartsProviders.getKey(screen);
		CcpJsonRepresentation providersRecord = JnEntityGrouperChartsProviders.ENTITY.getOneById(providersKey);
		List<String> providers = providersRecord.getAsStringList(JnEntityGrouperChartsProviders.Fields.providers);
		List<CcpJsonRepresentation> descriptions = new ArrayList<>();
		for (String provider : providers) {
			CcpStringDecorator providerName = new CcpStringDecorator(provider);
			CcpReflectionConstructorDecorator reflection = providerName.reflection();
			CcpBusiness providerBusiness = reflection.newInstance();
			CcpJsonRepresentation output = providerBusiness.execute(CcpOtherConstants.EMPTY_JSON);
			List<CcpJsonRepresentation> charts = output.getAsJsonList(JnGrouperChart.Fields.charts);
			descriptions.addAll(charts);
		}
		List<CcpJsonRepresentation> groupKeys = new ArrayList<>();
		for (CcpJsonRepresentation description : descriptions) {
			CcpJsonRepresentation groupKey = getGroupKey(description);
			groupKeys.add(groupKey);
		}
		CcpEntity mainGrouper = JnGroupers.getGrouper(false);
		CcpEntity twinGrouper = JnGroupers.getGrouper(true);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpUnionAllExecutor unionAllExecutor = crud.getUnionAllExecutor();
		CcpSelectUnionAll groups = unionAllExecutor.unionAll(groupKeys, mainGrouper, twinGrouper);
		CcpJsonRepresentation response = CcpOtherConstants.EMPTY_JSON;
		for (CcpJsonRepresentation description : descriptions) {
			CcpJsonRepresentation counters = getCounters(description, groups);
			String chart = description.getAsString(JnGrouperChart.Fields.chart);
			CcpFieldName chartField = new CcpFieldName(chart);
			boolean chartStarted = response.containsAllFields(chartField);
			CcpJsonRepresentation countersOfTheChart = chartStarted ? response.getInnerJson(chartField) : CcpOtherConstants.EMPTY_JSON;
			CcpJsonRepresentation merged = countersOfTheChart.mergeWithAnotherJson(counters);
			response = response.put(chartField, merged);
		}
		return response;
	}

	private static CcpJsonRepresentation getGroupKey(CcpJsonRepresentation description) {
		String parentName = description.getAsString(JnGrouperChart.Fields.parentName);
		String childName = description.getAsString(JnGrouperChart.Fields.childName);
		String parentId = description.getAsString(JnGrouperChart.Fields.parentId);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentName, childName, parentId);
		return groupKey;
	}

	/**
	 * The counters of the group of a description, as the chart takes them.
	 * @param description the description of the group
	 * @param groups the groups read on both sides
	 * @return {key: total}, or {sumAs: sum} when the description asks for the sum; empty when the group does not exist
	 */
	private static CcpJsonRepresentation getCounters(CcpJsonRepresentation description, CcpSelectUnionAll groups) {
		String sideName = description.getAsString(JnGrouperChart.Fields.side);
		CcpEntityPhase side = CcpEntityPhase.valueOf(sideName);
		CcpEntity grouper = side.getEntity(JnEntityGrouper.ENTITY);
		CcpEntityMetaData grouperDetails = grouper.getEntityMetaData();
		CcpJsonRepresentation groupKey = getGroupKey(description);
		String groupId = JnEntityGrouper.ENTITY.calculateId(groupKey);
		CcpJsonRepresentation group = groups.getEntityRow(grouperDetails.entityName, groupId);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		boolean noGroup = children.isEmpty();

		if(noGroup) {
			return CcpOtherConstants.EMPTY_JSON;
		}

		String countersText = children.get(0);
		CcpJsonRepresentation counters = new CcpJsonRepresentation(countersText);
		boolean keepTheKeys = false == description.containsAllFields(JnGrouperChart.Fields.sumAs);

		if(keepTheKeys) {
			return counters;
		}

		Set<String> keys = counters.fieldSet();
		long sum = 0;
		for (String key : keys) {
			CcpFieldName keyField = new CcpFieldName(key);
			Long total = counters.getAsLongNumber(keyField);
			sum += total;
		}
		String sumAs = description.getAsString(JnGrouperChart.Fields.sumAs);
		CcpFieldName sumAsField = new CcpFieldName(sumAs);
		CcpJsonRepresentation sumOfTheGroup = CcpOtherConstants.EMPTY_JSON.put(sumAsField, sum);
		return sumOfTheGroup;
	}
}
