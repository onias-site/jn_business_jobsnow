package com.jn.business.http;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.especifications.http.CcpErrorHttpClientPermanent;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.http.CcpHttpApiExecutor;
import com.ccp.especifications.http.CcpHttpRequester;
import java.util.function.Function;
import com.ccp.business.CcpBusiness;
import com.jn.entities.JnEntityHttpApiRetrySendRequest;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Executes HTTP calls by wrapping a CcpHttpApiExecutor and applies an automatic retry policy to server errors (5xx), with
 * a sleep between the attempts, until the maximum limit is reached, when the error is rethrown. Since 2026-11-27 the
 * failures are recorded by the call itself ({@link JnHttpApiErrors}), at each attempt, in {@code jn_http_api_error_client}
 * and {@code jn_http_api_error_server}: a client error (4xx) arrives here already recorded, as
 * {@link CcpErrorHttpClientPermanent}, and is rethrown at once.
 */
public class JnBusinessSendHttpRequest implements CcpBusiness{

	/** The external API, the {@code apiName} of the attempts. */
	public final JnHttpApiErrors.ExternalApi api;
	/** Turns any other failure into the result. */
	public final Function<Throwable, CcpJsonRepresentation> exceptionHandler;
	/** The call to the external API, with its retry policy. */
	public final CcpHttpApiExecutor processThatSendsHttpRequest;

	/**
	 * Wraps the call.
	 * @param api the external API
	 * @param processThatSendsHttpRequest the call to the external API
	 * @param exceptionHandler turns any other failure into the result
	 */
	public JnBusinessSendHttpRequest(JnHttpApiErrors.ExternalApi api, CcpHttpApiExecutor processThatSendsHttpRequest, Function<Throwable, CcpJsonRepresentation> exceptionHandler) {
		this.api = api;
		this.processThatSendsHttpRequest = processThatSendsHttpRequest;
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * Runs the call. A client error (4xx), already recorded, is rethrown; a server error (5xx) is retried (see
	 * {@code retryToSendIntantMessage}); any other failure goes to the exception handler.
	 * @param json the request
	 * @return the response, or the result of the exception handler
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		try {
			CcpJsonRepresentation response = this.processThatSendsHttpRequest.execute(json);
			return response;
		}catch (CcpErrorHttpServer e) {
			CcpJsonRepresentation attempt = this.getAttempt(e);
			CcpJsonRepresentation retryResponse = this.retryToSendIntantMessage(e, json, attempt);
			return retryResponse;
		}catch (CcpErrorHttpClientPermanent e) {
			throw e;
		}catch(Throwable e) {
			CcpJsonRepresentation handledError = this.exceptionHandler.apply(e);
			return handledError;
		}
	}

	/**
	 * Builds the key of the attempts of a request, as {@code jn_http_api_retry_send_request} requires it: the error (url,
	 * method, masked headers, request), the whole error as {@code details}, its {@code status} as {@code httpStatus} and the
	 * {@code apiName}. The {@code timestamp} and {@code date} are filled here, through the same transformer the entity
	 * declares: the validator of the entity runs before its transformer, so the required fields have to arrive filled.
	 * @param e the server error
	 * @return the key of the attempts
	 */
	private CcpJsonRepresentation getAttempt(CcpErrorHttpServer e) {
		String details = e.entity.asUgglyJson();
		Integer httpStatus = e.entity.getAsIntegerNumber(CcpHttpRequester.JsonFieldNames.status);
		String apiName = this.api.getValue();
		CcpJsonRepresentation errorWithDetails = e.entity.put(JnJsonCommonsFields.details, details);
		CcpJsonRepresentation errorWithHttpStatus = errorWithDetails.put(JnJsonCommonsFields.httpStatus, httpStatus);
		CcpJsonRepresentation errorWithApiName = errorWithHttpStatus.put(JnJsonCommonsFields.apiName, apiName);
		CcpJsonRepresentation attempt = JnJsonTransformersFieldsEntityDefault.timestamp.execute(errorWithApiName);
		return attempt;
	}

	/**
	 * Registers one more attempt of the request; when every attempt was used, rethrows the error (recorded by the call),
	 * otherwise sleeps and runs the call again.
	 * @param e the server error
	 * @param json the request
	 * @param attempt the key of the attempts
	 * @return the response of a later attempt
	 */
	private CcpJsonRepresentation retryToSendIntantMessage(CcpErrorHttpServer e, CcpJsonRepresentation json, CcpJsonRepresentation attempt) {
		Integer maxTries = this.processThatSendsHttpRequest.getMaxTries();
		String attemptsName = JnJsonCommonsFields.attempts.name();
		boolean exceededTries = JnEntityHttpApiRetrySendRequest.exceededTries(attempt, attemptsName, maxTries);

		if(exceededTries) {
			throw e;
		}

		Integer sleep = this.processThatSendsHttpRequest.getSleepTimeToRetry();
		CcpTimeDecorator timer = new CcpTimeDecorator();
		timer.sleep(sleep);
		CcpJsonRepresentation retryResponse = this.execute(json);
		return retryResponse;
	}

}
