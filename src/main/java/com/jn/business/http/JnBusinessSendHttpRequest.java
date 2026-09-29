package com.jn.business.http;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import java.util.function.Function;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Executes HTTP calls by wrapping a CcpHttpApiExecutor and applies an automatic
 * retry policy to server errors (5xx). Client errors (4xx) are recorded in
 * JnEntityHttpApiErrorClient and rethrown immediately; server errors trigger
 * controlled retries, with a sleep between them, until the maximum limit is reached,
 * at which point the error is recorded in JnEntityHttpApiErrorServer and rethrown.
 */
public class JnBusinessSendHttpRequest implements CcpBusiness{
	
	public final Function<Throwable, CcpJsonRepresentation> exceptionHandler;
	public final CcpHttpApiExecutor processThatSendsHttpRequest;

	public JnBusinessSendHttpRequest(CcpHttpApiExecutor processThatSendsHttpRequest, Function<Throwable, CcpJsonRepresentation> exceptionHandler) {
		this.processThatSendsHttpRequest = processThatSendsHttpRequest;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Executes the HTTP request. Catches CcpErrorHttpClient to save the error details
	 * and rethrow them; catches CcpErrorHttpServer to start the retry logic.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		try {
			CcpJsonRepresentation response = this.processThatSendsHttpRequest.execute(json);
			return response;
		}catch (CcpErrorHttpServer e) {
			String details = e.entity.asUgglyJson();
			CcpJsonRepresentation errorWithRequest = e.entity.mergeWithAnotherJson(json);
			CcpJsonRepresentation httpErrorDetails = errorWithRequest.put(JnJsonCommonsFields.details, details);
			CcpJsonRepresentation retryResponse = this.retryToSendIntantMessage(e, json, httpErrorDetails);
			return retryResponse;
		}catch (CcpErrorHttpClient e) {
			String details = e.entity.asUgglyJson();
			CcpJsonRepresentation errorWithRequest = e.entity.mergeWithAnotherJson(json);
			CcpJsonRepresentation httpErrorDetails = errorWithRequest.put(JnJsonCommonsFields.details, details);
			String request = httpErrorDetails.getAsString(JnJsonCommonsFields.request);
			httpErrorDetails = httpErrorDetails.put(JnJsonCommonsFields.request, request);
			JnEntityHttpApiErrorClient.ENTITY.save(httpErrorDetails);
			throw e;
		}catch(Throwable e) {
			CcpJsonRepresentation handledError = this.exceptionHandler.apply(e);
			return handledError;
		}
	}
	
	private CcpJsonRepresentation retryToSendIntantMessage(CcpErrorHttp e, CcpJsonRepresentation json, CcpJsonRepresentation httpErrorDetails) {
		Integer maxTries = this.processThatSendsHttpRequest.getMaxTries();
		String attemptsName = JnJsonCommonsFields.attempts.name();
		boolean exceededTries = JnEntityHttpApiRetrySendRequest.exceededTries(httpErrorDetails, attemptsName, maxTries);
		
		if(exceededTries) {
			JnEntityHttpApiErrorServer.ENTITY.save(httpErrorDetails);
			throw e;
		}
		
		Integer sleep = this.processThatSendsHttpRequest.getSleepTimeToRetry();
		CcpTimeDecorator timer = new CcpTimeDecorator();
		timer.sleep(sleep);
		CcpJsonRepresentation retryResponse = this.execute(json);
		return retryResponse;
	}

}
