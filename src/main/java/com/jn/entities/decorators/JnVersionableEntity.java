package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityVersionable;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Decorador que adiciona versionamento/auditoria a entidades marcadas com {@code @CcpEntityVersionable}.
 * A cada operação bulk, gera automaticamente um registro de histórico em {@code JnEntityVersionable}
 * com o estado anterior do JSON, a operação realizada, data e hora.
 */
public class JnVersionableEntity extends CcpDefaultEntityDelegator<CcpEntityVersionable>{
	
	public JnVersionableEntity(CcpEntity entity, Class<?> clazz) {
		super(entity, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
	}

	private final CcpBulkItem getVersionableToBulkOperationToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation versionable = this.getVersionableRecord(json, operation);
		String calculateId = JnEntityVersionable.ENTITY.calculateId(versionable);
		CcpBulkItem ccpBulkItem = new CcpBulkItem(versionable, CcpBulkEntityOperationType.create, JnEntityVersionable.ENTITY, calculateId);
				
		return ccpBulkItem;
	}

	private CcpJsonRepresentation getVersionableRecord(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpEntityMetaData entityDetails = this.entity.getEntityMetaData();
		
		CcpBusiness ifNotFound = x -> 
		
		{
			CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
			CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(handledJson);
			return onlyExistingFields;
		};
		
		CcpJsonRepresentation oneById = entityDetails.getOneByIdOrHandleItIfThisIdWasNotFound(json, ifNotFound);
		
		Supplier<CcpJsonRepresentation> jsonSupplier = oneById.getJsonSupplier();
		
		String id = entityDetails.getPrimaryKeyValues(jsonSupplier).asUgglyJson();
		
		String formattedDateTime = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS");
		
		CcpJsonRepresentation audit = 
				CcpOtherConstants.EMPTY_JSON
				.put(JnEntityVersionable.Fields.id, id)
				.put(JnEntityVersionable.Fields.json, "" + oneById)
				.put(JnEntityVersionable.Fields.operation, operation)
				.put(JnEntityVersionable.Fields.date, formattedDateTime)
				.put(JnEntityVersionable.Fields.entity, entityDetails.entityName)
				.put(JnEntityVersionable.Fields.timestamp, System.currentTimeMillis())
		;
		return audit;
	}



	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {

		//TODO FILA PARA EXCLUIR TODOS OS REGISTROS

		return json;
	}
	
	public List<CcpEntity> getAssociatedEntities() {
		List<CcpEntity> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity> result = new ArrayList<CcpEntity>(associatedEntities);
		result.add(JnEntityVersionable.ENTITY);
		return result;
	}
	
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation throwException = this.throwException();
		return throwException;
	}
	

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> asList = new ArrayList<>(bulkItems);
		
		CcpBulkItem versionableToBulkOperation = this.getVersionableToBulkOperationToBulkOperation(json, operation);
		asList.add(versionableToBulkOperation);
		return asList;
	}

	
}
