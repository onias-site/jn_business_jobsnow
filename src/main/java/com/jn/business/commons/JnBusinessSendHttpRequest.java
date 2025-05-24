package com.jn.business.commons;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.exceptions.http.CcpHttpClientError;
import com.ccp.exceptions.http.CcpHttpError;
import com.ccp.exceptions.http.CcpHttpServerError;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiParameters;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;

public class JnBusinessSendHttpRequest {

	public static final JnBusinessSendHttpRequest INSTANCE = new JnBusinessSendHttpRequest();
	private JnBusinessSendHttpRequest() {}
	public CcpJsonRepresentation execute(CcpJsonRepresentation json, Function<CcpJsonRepresentation, CcpJsonRepresentation> processThatSendsHttpRequest, JnBusinessHttpRequestType httpRequestType, String...keys) {

		CcpJsonRepresentation jsonWithApiName = json.put(JnEntityHttpApiParameters.Fields.apiName.name(), httpRequestType.name());
		CcpJsonRepresentation httpApiParameters = JnEntityHttpApiParameters.ENTITY.getOneById(jsonWithApiName);
		CcpJsonRepresentation jsonWithHttpApiParameters = json.putAll(httpApiParameters);
		
		try {
			CcpJsonRepresentation apply = processThatSendsHttpRequest.apply(jsonWithHttpApiParameters);
			return apply;
		}catch (CcpHttpServerError e) {
			String details = jsonWithHttpApiParameters.getJsonPiece(keys).asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.putAll(jsonWithHttpApiParameters).put(JnEntityHttpApiErrorClient.Fields.details.name(), details);
			CcpJsonRepresentation retryToSendIntantMessage = this.retryToSendIntantMessage(e, json, httpErrorDetails, processThatSendsHttpRequest, httpRequestType, keys);
			return retryToSendIntantMessage;
		}catch (CcpHttpClientError e) {
			String details = jsonWithHttpApiParameters.getJsonPiece(keys).asUgglyJson();
			CcpJsonRepresentation httpErrorDetails = e.entity.putAll(jsonWithHttpApiParameters).put(JnEntityHttpApiErrorClient.Fields.details.name(), details);
			String request = httpErrorDetails.getAsString(JnEntityHttpApiErrorClient.Fields.request.name());
			httpErrorDetails = httpErrorDetails.put(JnEntityHttpApiErrorClient.Fields.request.name(), request);
			JnEntityHttpApiErrorClient.ENTITY.createOrUpdate(httpErrorDetails);
			throw e;
		}
	}
	
	private CcpJsonRepresentation retryToSendIntantMessage(CcpHttpError e, CcpJsonRepresentation json, CcpJsonRepresentation httpErrorDetails, Function<CcpJsonRepresentation, CcpJsonRepresentation> processThatSendsHttpRequest, JnBusinessHttpRequestType httpRequestType, String... keys) {
		//LATER RENOMEAR ENTIDADES E CAMPOS
		Integer maxTries = httpErrorDetails.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.maxTries.name());
		boolean exceededTries = JnEntityHttpApiRetrySendRequest.exceededTries(httpErrorDetails, JnEntityHttpApiRetrySendRequest.Fields.tries.name(), maxTries);
		
		if(exceededTries) {
			JnEntityHttpApiErrorServer.ENTITY.createOrUpdate(httpErrorDetails);
			throw e;
		}
		
		Integer sleep = httpErrorDetails.getAsIntegerNumber(JnEntityHttpApiParameters.Fields.sleep.name());
		new CcpTimeDecorator().sleep(sleep);
		CcpJsonRepresentation execute = this.execute(json, processThatSendsHttpRequest, httpRequestType, keys);
		//DOUBT REMOVER TENTATIVAS
		//		JnAsyncBusinessRemoveTries.INSTANCE.apply(httpErrorDetails, "tries", 3, JnEntityHttpApiRetrySendRequest.INSTANCE);
		return execute;
	}

}
