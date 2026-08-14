package com.jn.services;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.service.CcpService;

/**
 * Interface base para todos os serviços do JobsNow. Fornece método default que carrega
 * automaticamente a classe de validação JSON pelo nome do valor do enum.
 *
 * Convenção: cada valor do enum tem um tipo homônimo <b>top-level</b> no mesmo pacote,
 * normalmente declarado como tipo secundário no próprio arquivo do serviço (ver
 * {@code ValidateLogin} em {@code JnServiceLogin.java}). Declarar esse tipo como classe
 * <i>interna</i> não funciona: {@code Class.forName} resolveria
 * {@code pacote.NomeDoServico$NomeDoValor}, e não {@code pacote.NomeDoValor}, resultando
 * em {@code JnErrorServiceValidationClassNotFound} em tempo de execução.
 */
public interface JnService extends CcpService {
	default Class<?> getJsonValidationClass() {
		
		Class<?> forName;
		try {
			forName = Class.forName(this.getClass().getPackageName() + "." + this.name());
		} catch (ClassNotFoundException e) {
			throw new JnErrorServiceValidationClassNotFound(e);
		}
		return forName;
	}

	default CcpJsonRepresentation execute(CcpJsonRepresentation json) {
		CcpJsonRepresentation execute = CcpService.super.execute(json);
		return execute;
	}
	

	@SuppressWarnings("serial")
	public static class JnErrorServiceValidationClassNotFound extends RuntimeException {
		private JnErrorServiceValidationClassNotFound(Throwable cause) {
			super(cause);
		}
	}
}
