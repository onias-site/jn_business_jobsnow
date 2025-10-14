package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpEntityBulkOperationType;
import com.ccp.especifications.db.utils.CcpEntity;
import com.ccp.especifications.db.utils.CcpEntityCrudOperationType;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityDecoratorFactory;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityDelegator;
import com.ccp.business.CcpBusiness;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityAudit;

public final class JnEntityVersionable extends CcpEntityDelegator implements CcpEntityDecoratorFactory {
	
	private JnEntityVersionable() {
		super(null);
	}
	
	protected JnEntityVersionable(CcpEntity entity) {
		super(entity);
	}
	
	private boolean isVersionableEntity() {
		List<String> primaryKeyNames = this.entity.getPrimaryKeyNames();
		int primaryKeyFieldsSize = primaryKeyNames.size();
		CcpEntityField[] fields = this.entity.getFields();
		boolean thisEntityHasMoreFieldsBesidesPrimaryKeys = primaryKeyFieldsSize < fields.length;
		return thisEntityHasMoreFieldsBesidesPrimaryKeys;
	}
	
	private final CcpBulkItem getVersionableToBulkOperationToBulkOperation(CcpJsonRepresentation json, CcpEntityBulkOperationType operation) {
		
		CcpJsonRepresentation audit = this.getAuditRecord(json, operation);
		CcpBulkItem ccpBulkItem = JnEntityAudit.ENTITY.getMainBulkItem(audit, CcpEntityBulkOperationType.create);
		return ccpBulkItem;
	}

	private CcpJsonRepresentation getAuditRecord(CcpJsonRepresentation json, CcpEntityBulkOperationType operation) {
		CcpJsonRepresentation oneById = this.entity.getOneById(json, x -> json);
		String id = this.entity.getPrimaryKeyValues(json).asUgglyJson();
		String entityName = this.entity.getEntityName();
		CcpJsonRepresentation audit = 
				CcpOtherConstants.EMPTY_JSON
				.put(JnEntityAudit.Fields.timestamp, System.currentTimeMillis())
				.put(JnEntityAudit.Fields.date, new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss.SSS"))
				.put(JnEntityAudit.Fields.operation, operation)
				.put(JnEntityAudit.Fields.entity, entityName)
				.put(JnEntityAudit.Fields.json, "" + oneById)
				.put(JnEntityAudit.Fields.id, id)
		;
		return audit;
	}
	

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		List<CcpBulkItem> bulkItems = this.toBulkItems(json, CcpEntityBulkOperationType.delete);
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems);
		return json;
	}
	
	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		CcpJsonRepresentation handledJson = this.getTransformedJsonByEachFieldInJson(json);
		this.validateJson(handledJson.putAll(json));
		CcpJsonRepresentation transformedJsonBeforeOperation = this.getTransformedJsonBeforeOperation(handledJson, CcpEntityCrudOperationType.save);
		CcpJsonRepresentation onlyExistingFields = this.getOnlyExistingFields(transformedJsonBeforeOperation);
	
		List<CcpBulkItem> bulkItems = this.toBulkItems(onlyExistingFields, CcpEntityBulkOperationType.create);
		JnExecuteBulkOperation.INSTANCE.executeBulk(bulkItems);
		CcpJsonRepresentation transformedJsonAfterOperation = this.getTransformedJsonAfterOperation(transformedJsonBeforeOperation, CcpEntityCrudOperationType.save);
		return transformedJsonAfterOperation;
	}

	public CcpEntity getEntity(CcpEntity entity) {
		JnEntityVersionable jnEntityVersionable = new JnEntityVersionable(entity);
		return jnEntityVersionable;
	}
	
	public CcpBusiness getOperationCallback(CcpEntityCrudOperationType operation){
		return json -> operation.execute(this, json);
	}

	public List<CcpBulkItem> toBulkItems(CcpJsonRepresentation json, CcpEntityBulkOperationType operation) {
		
		String calculateId = super.calculateId(json);
		CcpBulkItem mainBulkItem = new CcpBulkItem(json, operation, this, calculateId);
		List<CcpBulkItem> asList = new ArrayList<>();
		asList.add(mainBulkItem);
		
		boolean versionableEntity = this.isVersionableEntity();
	
		if(versionableEntity) {
			CcpBulkItem versionableToBulkOperation = this.getVersionableToBulkOperationToBulkOperation(json, operation);
			asList.add(versionableToBulkOperation);
		}

		return asList;
	}
	
	public CcpBulkItem getMainBulkItem(CcpJsonRepresentation json, CcpEntityBulkOperationType operation) {
		CcpBulkItem bulkItem = this.toBulkItems(json, operation).stream().filter(x -> x.entity.getEntityName().equals(this.entity.getEntityName())).findFirst().get();
		return bulkItem;
	}

}
