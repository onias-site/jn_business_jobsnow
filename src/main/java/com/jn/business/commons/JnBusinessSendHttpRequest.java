package com.jn.business.commons;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiParameters;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;

public class JnBusinessSendHttpRequest {

	public static final JnBusinessSendHttpRequest INSTANCE = new JnBusinessSendHttpRequest();
	private JnBusinessSendHttpRequest() {}
	public CcpJsonRepresentation execute(CcpJsonRepresentation json, Function<CcpJsonRepresentation, CcpJsonRepresentation> processThatSendsHttpRequest, JnBusinessHttpRequestType httpRequestType, String...keys) {

		CcpJsonRepresentation jsonWithApiName = json.put(JnEntityHttpApiParameters.Fields.apiName, httpRequestType);
		CcpJsonRepresentation httpApiParameters = JnEntityHttpApiParameters.ENTITY.getOneById(jsonWithApiName);
		CcpJsonRepresentation jsonWithHttpApiParameters = json.putAll(httpApiParameters);
		
		try {
			CcpJsonRepresentation apply = processThatSendsHttpRequest.apply(jsonWithHttpApiParameters);
			return apply;
		}catch (CcpErrorHttpServer e) {
			String details = jsonWithHttpApiParameters.getDynamicVersion().getJsonPiece(keys).asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.putAll(jsonWithHttpApiParameters).put(JnEntityHttpApiErrorClient.Fields.details, details);
			CcpJsonRepresentation retryToSendIntantMessage = this.retryToSendIntantMessage(e, json, httpErrorDetails, processThatSendsHttpRequest, httpRequestType, keys);
			return retryToSendIntantMessage;
		}catch (CcpErrorHttpClient e) {
			String details = jsonWithHttpApiParameters.getDynamicVersion().getJsonPiece(keys).asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.putAll(jsonWithHttpApiParameters).put(JnEntityHttpApiErrorClient.Fields.details, details);
			String request = httpErrorDetails.getAsString(JnEntityHttpApiErrorClient.Fields.request);
			httpErrorDetails = httpErrorDetails.put(JnEntityHttpApiErrorClient.Fields.request, request);
			JnEntityHttpApiErrorClient.ENTITY.createOrUpdate(httpErrorDetails);
			throw e;
		}
	}
	
	private CcpJsonRepresentation retryToSendIntantMessage(CcpErrorHttp e, CcpJsonRepresentation json, CcpJsonRepresentation httpErrorDetails, Function<CcpJsonRepresentation, CcpJsonRepresentation> processThatSendsHttpRequest, JnBusinessHttpRequestType httpRequestType, String... keys) {
		Integer maxTries = httpErrorDetails.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.maxTries);
		boolean exceededTries = JnEntityHttpApiRetrySendRequest.exceededTries(httpErrorDetails, JnEntityHttpApiRetrySendRequest.Fields.attempts.name(), maxTries);
		
		if(exceededTries) {
			JnEntityHttpApiErrorServer.ENTITY.createOrUpdate(httpErrorDetails);
			throw e;
		}
		
		Integer sleep = httpErrorDetails.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.sleep);
		new CcpTimeDecorator().sleep(sleep);
		CcpJsonRepresentation execute = this.execute(json, processThatSendsHttpRequest, httpRequestType, keys);
		//DOUBT REMOVER TENTATIVAS
		//		JnAsyncBusinessRemoveTries.INSTANCE.apply(httpErrorDetails, "tries", 3, JnEntityHttpApiRetrySendRequest.INSTANCE);
		return execute;
	}

}
