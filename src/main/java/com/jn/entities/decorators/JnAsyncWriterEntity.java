package com.jn.entities.decorators;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityDelegator;
import com.jn.mensageria.JnFunctionMensageriaSender;

public class JnAsyncWriterEntity extends CcpEntityDelegator  {

	public JnAsyncWriterEntity(CcpEntity entity, Class<?> clazz) {
		super(entity);
	}

	public CcpJsonRepresentation delete(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.delete);
		return apply;
	}

	public CcpJsonRepresentation deleteAnyWhere(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.deleteAnyWhere);
		return apply;
	}

	public CcpJsonRepresentation save(CcpJsonRepresentation json) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.save);
		return apply;
	}

	public CcpJsonRepresentation transferDataTo(CcpJsonRepresentation json, CcpEntity... entities) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.transferDataTo, entities);
		return apply;
	}

	public CcpJsonRepresentation copyDataTo(CcpJsonRepresentation json, CcpEntity... entities) {
		CcpJsonRepresentation apply = this.sendToMensageria(json, CcpEntityOperationType.copyDataTo, entities);
		return apply;
	}
	
	private CcpJsonRepresentation sendToMensageria(CcpJsonRepresentation json, CcpEntityOperationType operation, CcpEntity... entities) {
		Set<String> collectEntitiesNames = this.collectEntitiesNames(entities);
		CcpJsonRepresentation put = json.put(CcpEntityOperationType.Fields.entities, collectEntitiesNames);
		JnFunctionMensageriaSender sender = new JnFunctionMensageriaSender(this.entity, operation);
		CcpJsonRepresentation apply = sender.apply(put);
		return apply;
	}

	private Set<String> collectEntitiesNames(CcpEntity... entities){
		Set<String> collect = Arrays.asList(entities).stream()
		.map(x -> x.getEntityDetails().configurationClass.getName())
		.collect(Collectors.toSet());
		return collect;
	}
}
