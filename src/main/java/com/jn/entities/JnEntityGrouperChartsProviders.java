package com.jn.entities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionablePurge;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnGrouperChartsScreens;

/**
 * Who provides the charts of a screen read from the counters of {@code jn_grouper}: by screen
 * ({@link JnGrouperChartsScreens}), the class names of the businesses that describe the groups of each chart
 * ({@code JnGrouperChart}). The screen reads groups of several cost centers (vis and js), which do not import one
 * another: each one seeds its provider here, from the {@code getFirstRecordsToInsert} of one of its entities, and
 * {@code JnBusinessGetGrouperCharts} runs them by class name, the same way {@code jn_write_event_subscribers} does.
 * <p>
 * One record per screen, which every cost center fills: a seed adds its providers to the ones already recorded (an
 * atomic script of the database), so the seeds of two cost centers do not overwrite each other.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_grouper_charts_providers}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JnEntityGrouperChartsProviders.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityGrouperChartsProviders implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static JnEntityGrouperChartsProviders INSTANCE = new JnEntityGrouperChartsProviders();

	private JnEntityGrouperChartsProviders() {}

	/** The entity {@code jn_grouper_charts_providers}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityGrouperChartsProviders.class).entityInstance;

	/** The script of the seeds and its parameter. */
	enum Scripts implements CcpJsonFieldName {
		/** Adds each provider of the seed that is not recorded yet. */
		addProviders {
			public String getValue() {
				return "for (def p : params.providers) { if (false == ctx._source.providers.contains(p)) { ctx._source.providers.add(p); } }";
			}
		},
		/** Parameter: the class names of the providers of the seed. */
		providers,
	}

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code screen} field: the primary key (so required), the screen of the charts. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(allowedValuesEnum = JnGrouperChartsScreens.class)
		screen,
		/** The {@code providers} field: required, list, the class names of the businesses that describe the charts. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray(minSize = 1)
		@CcpJsonFieldTypeString(allowsEmptyString = false)
		providers,
		;
	}

	/**
	 * The key of the record of a screen.
	 * @param screen the screen
	 * @return the key, ready to be read by id
	 */
	public static CcpJsonRepresentation getKey(JnGrouperChartsScreens screen) {
		String screenName = screen.name();
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(Fields.screen, screenName);
		return key;
	}

	/**
	 * The seed of the providers of a cost center for a screen, to be returned by the {@code getFirstRecordsToInsert} of
	 * one of its entities.
	 * @param screen the screen
	 * @param providers the businesses of the cost center that describe its charts of the screen
	 * @return the bulk items of the record
	 */
	@SafeVarargs
	public static List<CcpBulkItem> getSeeds(JnGrouperChartsScreens screen, Class<? extends CcpBusiness>... providers) {
		Stream<Class<? extends CcpBusiness>> providersStream = Arrays.stream(providers);
		Stream<String> providerNamesStream = providersStream.map(provider -> provider.getName());
		List<String> providerNames = providerNamesStream.collect(Collectors.toList());
		CcpJsonRepresentation key = getKey(screen);
		CcpJsonRepresentation record = key.put(Fields.providers, providerNames);
		String script = Scripts.addProviders.getValue();
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON.put(Scripts.providers, providerNames);
		CcpJsonRepresentation itemWithScript = CcpOtherConstants.EMPTY_JSON.put(CcpBulkScriptedUpdateFields.script, script);
		CcpJsonRepresentation itemWithParams = itemWithScript.put(CcpBulkScriptedUpdateFields.params, params);
		// the first seed of the screen creates the record as it is
		CcpJsonRepresentation itemWithUpsert = itemWithParams.put(CcpBulkScriptedUpdateFields.upsert, record);
		CcpJsonRepresentation item = itemWithUpsert.put(CcpBulkScriptedUpdateFields.scriptedUpsert, false);
		String recordId = ENTITY.calculateId(key);
		CcpBulkItem seed = new CcpBulkItem(item, CcpBulkEntityOperationType.scriptedUpdate, ENTITY, recordId);
		List<CcpBulkItem> seeds = Arrays.asList(seed);
		return seeds;
	}
}
