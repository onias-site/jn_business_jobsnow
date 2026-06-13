package com.jn.business.http;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;

/**
 * Executa chamadas HTTP encapsulando um CcpHttpApiExecutor e aplica política de
 * retentativa automática para erros de servidor (5xx). Erros de cliente (4xx) são
 * registrados em JnEntityHttpApiErrorClient e relançados imediatamente; erros de
 * servidor disparam novas tentativas controladas, com sleep entre elas, até atingir
 * o limite máximo, quando então o erro é registrado em JnEntityHttpApiErrorServer e
 * relançado.
 */
public class JnBusinessSendHttpRequest implements CcpBusiness{
	
	private final CcpHttpApiExecutor processThatSendsHttpRequest;
	

	public JnBusinessSendHttpRequest(CcpHttpApiExecutor processThatSendsHttpRequest) {
		this.processThatSendsHttpRequest = processThatSendsHttpRequest;
	}

	/**
	 * Executa a requisição HTTP. Captura CcpErrorHttpClient para salvar o detalhe
	 * do erro e relançar; captura CcpErrorHttpServer para iniciar a lógica de retry.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		try {
			CcpJsonRepresentation apply = this.processThatSendsHttpRequest.execute(json);
			return apply;
		}catch (CcpErrorHttpServer e) {
			String details = e.entity.asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.mergeWithAnotherJson(json).put(JnEntityHttpApiErrorClient.Fields.details, details);
			CcpJsonRepresentation retryToSendIntantMessage = this.retryToSendIntantMessage(e, json, httpErrorDetails);
			return retryToSendIntantMessage;
		}catch (CcpErrorHttpClient e) {
			String details = e.entity.asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.mergeWithAnotherJson(json).put(JnEntityHttpApiErrorClient.Fields.details, details);
			String request = httpErrorDetails.getAsString(JnEntityHttpApiErrorClient.Fields.request);
			httpErrorDetails = httpErrorDetails.put(JnEntityHttpApiErrorClient.Fields.request, request);
			JnEntityHttpApiErrorClient.ENTITY.save(httpErrorDetails);
			throw e;
		}
	}
	
	private CcpJsonRepresentation retryToSendIntantMessage(CcpErrorHttp e, CcpJsonRepresentation json, CcpJsonRepresentation httpErrorDetails) {
		Integer maxTries = this.processThatSendsHttpRequest.getMaxTries();
		boolean exceededTries = JnEntityHttpApiRetrySendRequest.exceededTries(httpErrorDetails, JnEntityHttpApiRetrySendRequest.Fields.attempts.name(), maxTries);
		
		if(exceededTries) {
			JnEntityHttpApiErrorServer.ENTITY.save(httpErrorDetails);
			throw e;
		}
		
		Integer sleep = this.processThatSendsHttpRequest.getSleepTimeToRetry();
		new CcpTimeDecorator().sleep(sleep);
		CcpJsonRepresentation execute = this.execute(json);
		return execute;
	}

}
