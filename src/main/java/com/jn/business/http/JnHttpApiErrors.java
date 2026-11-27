package com.jn.business.http;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.http.CcpErrorHttp;
import com.ccp.especifications.http.CcpErrorHttpClient;
import com.ccp.especifications.http.CcpErrorHttpClientPermanent;
import com.ccp.especifications.http.CcpErrorHttpServer;
import com.ccp.especifications.http.CcpHttpRequester;
import com.jn.entities.JnEntityHttpApiErrorClient;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;

/**
 * Records the HTTP failures of the external APIs that the platform does not host (since 2026-11-27: the instant messenger
 * and the e-mail provider; the Elasticsearch is hosted by the platform and stays out): a client error (4xx) goes to
 * {@code jn_http_api_error_client}, a server error (5xx) to {@code jn_http_api_error_server}, each one with the moment of
 * the failure. Every call to those APIs catches its {@link CcpErrorHttp} and throws what {@link #record} returns: the
 * client error wrapped in {@link CcpErrorHttpClientPermanent} (no new attempt fixes it, and the consumer of the messaging
 * acknowledges the message), the server error as it came (it may be retried). The statuses that the call maps to errors of
 * its own (a bot blocked by the user, too many requests) are not {@link CcpErrorHttp} and are not recorded here.
 */
public class JnHttpApiErrors {

	/** The external APIs whose failures are recorded, by the {@code apiName} of the record. */
	public static enum ExternalApi implements CcpJsonFieldName {
		/** The instant messenger (Telegram). */
		telegram,
		/** The e-mail provider (SendGrid). */
		sendgrid,
	}

	private JnHttpApiErrors() {}

	/**
	 * Records the failure in the entity of its kind and returns the error to throw. A failure to record is printed and
	 * never hides the failure of the API.
	 * @param api the external API
	 * @param error the HTTP error of the call
	 * @return the client error wrapped as permanent, or the error itself
	 */
	public static RuntimeException record(ExternalApi api, CcpErrorHttp error) {
		boolean clientError = error instanceof CcpErrorHttpClient;

		if(clientError) {
			saveTheRecord(JnEntityHttpApiErrorClient.ENTITY, api, error);
			CcpErrorHttpClientPermanent permanentError = new CcpErrorHttpClientPermanent((CcpErrorHttpClient) error);
			return permanentError;
		}

		boolean serverError = error instanceof CcpErrorHttpServer;

		if(serverError) {
			saveTheRecord(JnEntityHttpApiErrorServer.ENTITY, api, error);
			return error;
		}

		return error;
	}

	/**
	 * Saves the record of the failure: the error (url, method, masked headers, request, response), the whole error as
	 * {@code details}, its {@code status} as {@code httpStatus}, the {@code apiName} and the moment of the failure
	 * ({@code timestamp} and {@code date}, filled here because the validator of the entity runs before its transformer).
	 * @param entity the entity of the kind of failure
	 * @param api the external API
	 * @param error the HTTP error
	 */
	private static void saveTheRecord(CcpEntity entity, ExternalApi api, CcpErrorHttp error) {
		try {
			String details = error.entity.asUgglyJson();
			Integer httpStatus = error.entity.getAsIntegerNumber(CcpHttpRequester.JsonFieldNames.status);
			String apiName = api.getValue();
			CcpJsonRepresentation errorWithDetails = error.entity.put(JnJsonCommonsFields.details, details);
			CcpJsonRepresentation errorWithHttpStatus = errorWithDetails.put(JnJsonCommonsFields.httpStatus, httpStatus);
			CcpJsonRepresentation errorWithApiName = errorWithHttpStatus.put(JnJsonCommonsFields.apiName, apiName);
			CcpJsonRepresentation record = JnJsonTransformersFieldsEntityDefault.timestamp.execute(errorWithApiName);
			entity.save(record);
		} catch (RuntimeException recordFailure) {
			recordFailure.printStackTrace();
		}
	}
}
