package com.jn.mensageria;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityWriteEventSubscribers;

/**
 * Action of the procedure of {@link JnBusinessPublishWriteEvent}: publishes the written record to the messaging once per
 * subscriber found in {@link JnEntityWriteEventSubscribers} (the topic is the class name of the subscriber).
 */
public class JnBusinessPublishToTheWriteEventSubscribers implements CcpBusiness {

	/** The written record, published to each subscriber. */
	private final CcpJsonRepresentation writtenRecord;

	/**
	 * Keeps the record to publish.
	 * @param writtenRecord the written record
	 */
	public JnBusinessPublishToTheWriteEventSubscribers(CcpJsonRepresentation writtenRecord) {
		this.writtenRecord = writtenRecord;
	}

	/**
	 * Publishes the written record to each subscriber of the event.
	 * @param jsonWithTheSubscribers the JSON of the procedure, holding the subscribers record under {@code _entities}
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation jsonWithTheSubscribers) {
		CcpJsonRepresentation eventSubscribers = jsonWithTheSubscribers.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, JnEntityWriteEventSubscribers.ENTITY);
		List<String> subscriberNames = eventSubscribers.getAsStringList(JnEntityWriteEventSubscribers.Fields.subscribers);
		for (String subscriberName : subscriberNames) {
			CcpStringDecorator subscriberNameDecorator = new CcpStringDecorator(subscriberName);
			CcpReflectionConstructorDecorator reflection = subscriberNameDecorator.reflection();
			CcpBusiness subscriber = reflection.newInstance();
			JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(subscriber);
			sender.execute(this.writtenRecord);
		}
		return jsonWithTheSubscribers;
	}
}
