package com.jn.entities.decorators2;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity2;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityVersionable;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpDefaultEntityDelegator;
import com.ccp.especifications.db.utils.entity.decorators.engine2.CcpEntityDetails;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAudit;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnVersionableEntity extends CcpDefaultEntityDelegator<CcpEntityVersionable>{
	
	final Class<?>  clazz;
	
	public JnVersionableEntity(CcpEntity2 entity, Class<?> clazz) {
		super(entity, 2, JnExecuteBulkOperation.INSTANCE, JnDeleteKeysFromCache.INSTANCE);
		this.clazz = clazz;
	}
	
	public boolean isThisEntityDecorated(Class<CcpEntityVersionable> annotation) {
		boolean annotationPresent = this.clazz.isAnnotationPresent(annotation);
		return annotationPresent;
	}

	public CcpEntityVersionable getAnnotation() {
		CcpEntityVersionable annotation = this.clazz.getAnnotation(CcpEntityVersionable.class);
		return annotation;
	}
	
	private boolean isVersionableEntity() {
		CcpEntityDetails entityDetails = this.getEntityDetails();
		int primaryKeyFieldsSize = entityDetails.primaryKeyNames.size();
		boolean thisEntityHasMoreFieldsBesidesPrimaryKeys = primaryKeyFieldsSize < entityDetails.allFields.length;
		return thisEntityHasMoreFieldsBesidesPrimaryKeys;
	}
	
	private final CcpBulkItem getVersionableToBulkOperationToBulkOperation(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		
		CcpJsonRepresentation audit = this.getAuditRecord(json, operation);
		CcpBulkItem ccpBulkItem = JnEntityAudit.ENTITY.toBulkItem(audit, CcpBulkEntityOperationType.create);
		return ccpBulkItem;
	}

	private CcpJsonRepresentation getAuditRecord(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		CcpJsonRepresentation oneById = this.entity.getOneByIdOrHandleItIfThisIdWasNotFound(json, x -> json);
		CcpEntityDetails entityDetails = this.entity.getEntityDetails();
		String id = entityDetails.getPrimaryKeyValues(json).asUgglyJson();
		CcpJsonRepresentation audit = 
				CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAudit.Fields.timestamp, System.currentTimeMillis())
				.put(JnEntityAudit.Fields.date, new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS"))
				.put(JnEntityAudit.Fields.operation, operation)
				.put(JnEntityAudit.Fields.entity, entityDetails.entityName)
				.put(JnEntityAudit.Fields.json, "" + oneById)
				.put(JnEntityAudit.Fields.id, id)
		;
		return audit;
	}


	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {

		//TODO FILA PARA EXCLUIR TODOS OS REGISTROS

		return json;
	}
	
	public List<CcpEntity2> getAssociatedEntities() {
		List<CcpEntity2> associatedEntities = this.entity.getAssociatedEntities();
		ArrayList<CcpEntity2> result = new ArrayList<CcpEntity2>(associatedEntities);
		//FIXME
		//		result.add(JnEntityAudit.ENTITY);
		return result;
	}
	
	public CcpJsonRepresentation getOneByIdAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation throwException = this.throwException();
		return throwException;
	}
	

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpBulkEntityOperationType operation) {
		//FIXME
		List<CcpBulkItem> bulkItems = this.entity.toBulkItems(json, operation);
		List<CcpBulkItem> asList = new ArrayList<>(bulkItems);
		
		boolean versionableEntity = this.isVersionableEntity();
	
		if(versionableEntity) {
			CcpBulkItem versionableToBulkOperation = this.getVersionableToBulkOperationToBulkOperation(json, operation);
			asList.add(versionableToBulkOperation);
		}
		
		return asList;
	}

	
}
