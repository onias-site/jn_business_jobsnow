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
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsTransformer;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionablePurge;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorRequired;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.entities.decorators.engine.JnVersionableEntity;
import com.jn.entities.decorators.engine.JnVersionablePurgeEntity;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * Who listens to a write of an entity: by entity name and write operation ({@code insert}, {@code update},
 * {@code save}, {@code delete}...), the class names of the businesses that run, through the messaging, after that write.
 * The entity that writes announces it with {@code JnBusinessPublishWriteEvent} and never knows its listeners; the cost
 * center that listens seeds its record here, from the {@code getFirstRecordsToInsert} of one of its entities, with its
 * own classes. That is how a write of js reaches vis without either of them importing the other, the same way the bot
 * engine (jb) runs the steps of vis by class name.
 * <p>
 * One record per event: a second cost center that listens to the same event adds its business to the
 * {@code subscribers} of the same seed, since two seeds with the same id would collide.
 * <p>
 * Configuration:
 * <ul>
 * <li>index {@code jn_write_event_subscribers}</li>
 * <li>records cached for 3600 seconds</li>
 * <li>versionable: every write keeps the previous state in {@code jn_versionable}</li>
 * </ul>
 */
@CcpEntityCache(3600)
@CcpEntityVersionable(JnVersionableEntity.class)
@CcpEntityVersionablePurge(JnVersionablePurgeEntity.class)
@CcpEntityFieldsValidator(JnEntityWriteEventSubscribers.Fields.class)
@CcpEntityFieldsTransformer(JnJsonTransformersFieldsEntityDefault.class)
public class JnEntityWriteEventSubscribers implements CcpEntityConfigurator {

	/** The entity {@code jn_write_event_subscribers}, with every decorator of this configuration. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntityWriteEventSubscribers.class).entityInstance;

	/**
	 * The fields of the entity, with their validation rules (this enum is the class named by
	 * {@code @CcpEntityFieldsValidator}).
	 */
	public static enum Fields implements CcpJsonFieldName{
		/** The {@code entityName} field: part of the primary key, required, text, the entity that is written. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(minLength = 1, maxLength = 100)
		entityName,
		/** The {@code writeOperation} field: part of the primary key, required, text, the write listened to. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldTypeString(allowedValuesEnum = CcpEntityDecoratorOperationType.class)
		writeOperation,
		/** The {@code subscribers} field: required, list, the class names of the businesses that run after the write. */
		@CcpJsonFieldValidatorRequired
		@CcpJsonFieldValidatorArray(minSize = 1)
		@CcpJsonFieldTypeString(allowsEmptyString = false)
		subscribers,
		;
	}

	/**
	 * The key of the event: the entity name and the write operation.
	 * @param entity the entity that is written
	 * @param writeOperation the write listened to
	 * @return the key, ready to be read by id
	 */
	public static CcpJsonRepresentation getEventKey(CcpEntity entity, CcpEntityDecoratorOperationType writeOperation) {
		CcpEntityMetaData entityMetaData = entity.getEntityMetaData();
		CcpJsonRepresentation eventKey = getEventKey(entityMetaData.entityName, writeOperation);
		return eventKey;
	}

	/**
	 * The key of the event: the entity name and the write operation.
	 * @param entityName the name of the entity that is written
	 * @param writeOperation the write listened to
	 * @return the key, ready to be read by id
	 */
	private static CcpJsonRepresentation getEventKey(String entityName, CcpEntityDecoratorOperationType writeOperation) {
		String writeOperationName = writeOperation.name();
		CcpJsonRepresentation keyWithEntityName = CcpOtherConstants.EMPTY_JSON.put(Fields.entityName, entityName);
		CcpJsonRepresentation eventKey = keyWithEntityName.put(Fields.writeOperation, writeOperationName);
		return eventKey;
	}

	/**
	 * The seed of an event, to be returned by the {@code getFirstRecordsToInsert} of an entity of the cost center that
	 * listens to it. The entity is given by its name, which is a contract (the index), because the listener may belong
	 * to a cost center that can not import the entity (vis listening to js).
	 * @param entityName the name of the entity that is written, as {@code getValue()}
	 * @param writeOperation the write listened to
	 * @param subscribers the businesses that run after the write, in the cost center that listens
	 * @return the bulk items of the record
	 */
	@SafeVarargs
	public static List<CcpBulkItem> getSeeds(CcpJsonFieldName entityName, CcpEntityDecoratorOperationType writeOperation, Class<? extends CcpBusiness>... subscribers) {
		Stream<Class<? extends CcpBusiness>> subscribersStream = Arrays.stream(subscribers);
		Stream<String> subscriberNamesStream = subscribersStream.map(subscriber -> subscriber.getName());
		List<String> subscriberNames = subscriberNamesStream.collect(Collectors.toList());
		String entityNameValue = entityName.getValue();
		CcpJsonRepresentation eventKey = getEventKey(entityNameValue, writeOperation);
		CcpJsonRepresentation eventSubscribers = eventKey.put(Fields.subscribers, subscriberNames);
		List<CcpBulkItem> seeds = ENTITY.toBulkItems(eventSubscribers, CcpBulkEntityOperationType.create);
		return seeds;
	}
}
