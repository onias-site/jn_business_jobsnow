package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDetails;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityVersionable;
import com.jn.utils.JnDeleteKeysFromCache;

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
		CcpEntityDetails entityDetails = this.entity.getEntityDetails();
		CcpJsonRepresentation oneById = this.entity.getEntityDetails().getOneByIdOrHandleItIfThisIdWasNotFound(json, x -> 
		
		{
			CcpJsonRepresentation handledJson = entityDetails.entity.getHandledJson(json);
			CcpJsonRepresentation onlyExistingFields = entityDetails.getOnlyExistingFields(handledJson);
			return onlyExistingFields;
		});
		String id = entityDetails.getPrimaryKeyValues(oneById).asUgglyJson();
		CcpJsonRepresentation audit = 
				CcpOtherConstants.EMPTY_JSON
				.put(JnEntityVersionable.Fields.timestamp, System.currentTimeMillis())
				.put(JnEntityVersionable.Fields.date, new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS"))
				.put(JnEntityVersionable.Fields.operation, operation)
				.put(JnEntityVersionable.Fields.entity, entityDetails.entityName)
				.put(JnEntityVersionable.Fields.json, "" + oneById)
				.put(JnEntityVersionable.Fields.id, id)
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
