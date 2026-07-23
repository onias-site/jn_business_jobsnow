package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.service.CcpService;

/**
 * Interface base para todos os serviços do JobsNow. Fornece método default que carrega
 * automaticamente a classe de validação JSON a partir de uma classe interna do mesmo pacote com o
 * nome do valor do enum (convenção: cada valor do enum tem uma classe interna homônima).
 */
public interface JnService extends CcpService {
	default Class<?> getJsonValidationClass() {
		Class<?> forName;
		try {
			forName = Class.forName(this.getClass().getPackageName() + "." + this.name());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return forName;
	}

	default CcpJsonRepresentation execute(CcpJsonRepresentation json) {
		CcpJsonRepresentation execute = CcpService.super.execute(json);
		return execute;
	}
	
}
