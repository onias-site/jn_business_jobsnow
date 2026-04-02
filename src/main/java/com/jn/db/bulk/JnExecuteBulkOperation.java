package com.jn.db.bulk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkExecutor;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.utils.JnDeleteKeysFromCache;

public class JnExecuteBulkOperation implements CcpExecuteBulkOperation{

	public static final JnExecuteBulkOperation INSTANCE = new JnExecuteBulkOperation();
	
	private JnExecuteBulkOperation() {}
	
	public JnExecuteBulkOperation executeBulk(Collection<CcpBulkItem> bulkItems,  Consumer<String[]> functionToDeleteKeysInTheCache) {
		
		List<CcpBulkItem> items = bulkItems.stream().filter(item -> item.operation.persistable).collect(Collectors.toList());
		
		boolean emptyItems = items.isEmpty();
		
		if(emptyItems) {
			return this;
		}

		CcpBulkExecutor dbBulkExecutor = CcpDependencyInjection.getDependency(CcpBulkExecutor.class);
		
		for (CcpBulkItem item : items) {
			dbBulkExecutor = dbBulkExecutor.addRecord(item);
		}
 		JnExecuteBulkOperation commitAndSaveErrorsAndDeleteRecordsFromCache = this.commitAndSaveErrorsAndDeleteRecordsFromCache(dbBulkExecutor, functionToDeleteKeysInTheCache);
		return commitAndSaveErrorsAndDeleteRecordsFromCache;
	}
	
	private JnExecuteBulkOperation commitAndSaveErrorsAndDeleteRecordsFromCache(CcpBulkExecutor dbBulkExecutor, Consumer<String[]> functionToDeleteKeysInTheCache) {

		List<CcpBulkOperationResult> allResults = dbBulkExecutor.getBulkOperationResult();
		List<CcpBulkOperationResult> errors = allResults.stream().filter(x -> x.hasError()).collect(Collectors.toList());
		List<CcpBulkItem> collect = errors.stream().map(x -> x.getReprocess(FunctionReprocessMapper.INSTANCE, JnEntityRecordToReprocess.ENTITY)).collect(Collectors.toList());
		this.executeBulk(collect, functionToDeleteKeysInTheCache);
		JnExecuteBulkOperation deleteKeysFromCache = this.deleteKeysFromCache(allResults);
		return deleteKeysFromCache; 
	}

	private JnExecuteBulkOperation deleteKeysFromCache(List<CcpBulkOperationResult> allResults) {
		Set<String> keysToDeleteInCache = new ArrayList<>(allResults).stream()
		.filter(x -> false == x.hasError())
		.map(x -> x.getCacheKey())
		.collect(Collectors.toSet());
		String[] array = keysToDeleteInCache.toArray(new String[keysToDeleteInCache.size()]);
		
		JnDeleteKeysFromCache.INSTANCE.accept(array);
		return this;
	}
	
	public JnExecuteBulkOperation executeBulk(CcpJsonRepresentation json, CcpBulkEntityOperationType operation,  Consumer<String[]> functionToDeleteKeysInTheCache, CcpEntity...entities) {
		
		List<CcpBulkItem> items = new ArrayList<>();
 		
		for (CcpEntity entity : entities) {
			List<CcpBulkItem> bulkItems = entity.toBulkItems(json, operation);
			for (CcpBulkItem bulkItem : bulkItems) {
				items.add(bulkItem);
			}			
		}
		
		JnExecuteBulkOperation executeBulk = this.executeBulk(items, functionToDeleteKeysInTheCache);
		return executeBulk;
	}
}
