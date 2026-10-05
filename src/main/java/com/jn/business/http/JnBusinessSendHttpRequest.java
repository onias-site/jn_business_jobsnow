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
	
	/** Turns any other failure into the result. */
	public final Function<Throwable, CcpJsonRepresentation> exceptionHandler;
	/** The call to the external API, with its retry policy. */
	public final CcpHttpApiExecutor processThatSendsHttpRequest;

	/**
	 * Wraps the call.
	 * @param processThatSendsHttpRequest the call to the external API
	 * @param exceptionHandler turns any other failure into the result
	 */
	public JnBusinessSendHttpRequest(CcpHttpApiExecutor processThatSendsHttpRequest, Function<Throwable, CcpJsonRepresentation> exceptionHandler) {
		this.processThatSendsHttpRequest = processThatSendsHttpRequest;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Runs the call. A client error (4xx) is recorded in {@code jn_http_api_error_client} and rethrown; a server error
	 * (5xx) is retried (see {@code retryToSendIntantMessage}); any other failure goes to the exception handler.
	 * @param json the request
	 * @return the response, or the result of the exception handler
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
	
	/**
	 * Registers one more attempt of the request; when every attempt was used, records the error in
	 * {@code jn_http_api_error_server} and rethrows it, otherwise sleeps and runs the call again.
	 * @param e the server error
	 * @param json the request
	 * @param httpErrorDetails the error details plus the request
	 * @return the response of a later attempt
	 */
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
