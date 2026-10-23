package com.jn.mensageria;

import java.util.function.Consumer;

import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDecoratorTypes;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Receives the messages of messaging and runs, for each one, the business or the entity operation named by the topic
 * (and the {@code operation} field), through {@link CcpMensageriaReceiver#executeProcess}.
 */
public class JnMensageriaReceiver extends CcpMensageriaReceiver{

	/** Singleton; use {@link #INSTANCE}. The operation is read from the {@code operation} field. */
	private JnMensageriaReceiver() {
		super(JnJsonCommonsFields.operation.name());
	}

	/** The single instance. */
	public static final JnMensageriaReceiver INSTANCE = new JnMensageriaReceiver();

	/**
	 * The bulk executor of the jn cost center.
	 * @return {@link JnExecuteBulkOperation#INSTANCE}
	 */
	public CcpExecuteBulkOperation getExecuteBulkOperation() {
		return JnExecuteBulkOperation.INSTANCE;
	}

	/**
	 * The cache cleanup of the jn cost center.
	 * @return {@link JnDeleteKeysFromCache#INSTANCE}
	 */
	public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
		return JnDeleteKeysFromCache.INSTANCE;
	}

	/**
	 * Builds the entity of the configurator without the asynchronous writer, so the consumer writes for real.
	 * @param newInstance the configurator
	 * @return the entity
	 */
	protected CcpEntity getCustomEntity(Object newInstance) {
		CcpEntityConfigurator configurator = (CcpEntityConfigurator)newInstance;
		CcpEntity entity = CcpEntityFactory.getCustomEntity(configurator, CcpEntityDecoratorTypes.AsyncWriter);
		return entity;
	}

	/**
	 * Returns the twin of the entity without the asynchronous writer.
	 * @param entity the entity
	 * @return the twin
	 */
	protected CcpEntity getTwinEntity(CcpEntity entity) {
		CcpEntity twinEntity = entity.getTwinEntity(CcpEntityDecoratorTypes.AsyncWriter);
		return twinEntity;
	}
}
