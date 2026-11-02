package com.jn.business.email.suppression;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.utils.JnSystemProperties;

/**
 * Brings the suppression lists of the e-mail provider to the database, as part of the daily scheduled process:
 * {@code bounces}, {@code invalid_emails} and {@code spam_reports} go to {@code jn_email_suppressed}, which stops the
 * sendings, and {@code blocks} to {@code jn_email_temporarily_blocked}, only for diagnosis.
 * <p>
 * Each record goes through {@code save}, and not through a bulk, because the bulk goes around the decorators, and the first
 * write of a suppressed address must be announced. The load reads the last days again on purpose: writing a record twice
 * changes nothing, and a day without the process loses nothing. A failure of the provider raises an error and the next load
 * reads the same period. An address the entity refuses is counted and left out: {@code invalid_emails} may hold malformed
 * addresses, which the sending refuses anyway. A suppression removed at the provider stays recorded here.
 * <p>
 * Input: {@code startTimeInSeconds} (optional), the moment to read from, which the first load gives as {@code 1} to bring
 * the whole lists; by default, the last {@value #DAYS_TO_READ_AGAIN} days. Output: the input plus {@code savedSuppressions}
 * and {@code ignoredSuppressions}.
 */
public class JnBusinessLoadEmailSuppressions implements CcpBusiness {

	/** The single instance: the business keeps no state. */
	public static final JnBusinessLoadEmailSuppressions INSTANCE = new JnBusinessLoadEmailSuppressions();

	/** How many days each load reads again by default. */
	private static final int DAYS_TO_READ_AGAIN = 7;

	private static final int SECONDS_IN_A_DAY = 86_400;

	private JnBusinessLoadEmailSuppressions() {}

	/** The fields of the input and of the output. */
	public static enum Fields implements CcpJsonFieldName {
		/** The moment to read from, as a Unix timestamp in seconds. */
		startTimeInSeconds,
		/** How many records were written. */
		savedSuppressions,
		/** How many records the entities refused. */
		ignoredSuppressions,
	}

	/**
	 * Reads every list from the moment asked and writes its records.
	 * @param json the input
	 * @return the input plus the counters
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpEmailProvider emailProvider = CcpDependencyInjection.getDependency(CcpEmailProvider.class);
		String providerToken = JnSystemProperties.INSTANCE.tokenEmailValue();
		String providerUrl = JnSystemProperties.INSTANCE.urlEmailSuppressionValue();
		long startTimeInSeconds = this.getStartTimeInSeconds(json);
		int savedSuppressions = 0;
		int ignoredSuppressions = 0;

		for (JnEmailSuppressionTarget target : JnEmailSuppressionTarget.values()) {
			List<CcpJsonRepresentation> records = emailProvider.getSuppressedEmailsCreatedSince(providerToken, providerUrl, target.suppressionList, startTimeInSeconds);
			CcpEntity entity = target.getEntity();
			for (CcpJsonRepresentation record : records) {
				try {
					CcpJsonRepresentation suppression = this.getSuppression(record, target);
					entity.save(suppression);
					savedSuppressions++;
				} catch (CcpJsonValidationError e) {
					ignoredSuppressions++;
				}
			}
		}
		CcpJsonRepresentation jsonWithSaved = json.put(Fields.savedSuppressions, savedSuppressions);
		CcpJsonRepresentation jsonWithCounters = jsonWithSaved.put(Fields.ignoredSuppressions, ignoredSuppressions);
		return jsonWithCounters;
	}

	/**
	 * The moment to read from: the one asked, or the last days.
	 * @param json the input
	 * @return the moment, as a Unix timestamp in seconds
	 */
	private long getStartTimeInSeconds(CcpJsonRepresentation json) {
		boolean startTimeWasAsked = json.containsField(Fields.startTimeInSeconds);

		if(startTimeWasAsked) {
			Long askedStartTime = json.getAsLongNumber(Fields.startTimeInSeconds);
			return askedStartTime;
		}
		long nowInSeconds = System.currentTimeMillis() / 1000;
		long defaultStartTime = nowInSeconds - DAYS_TO_READ_AGAIN * SECONDS_IN_A_DAY;
		return defaultStartTime;
	}

	/**
	 * Turns a record of the provider into a record of the entity: the address in lower case, as the sendings hash it, the
	 * moment as a whole number, and no empty text, which the validation refuses in an optional field.
	 * @param record the record of the provider
	 * @param target the list the record came from
	 * @return the record to write
	 */
	private CcpJsonRepresentation getSuppression(CcpJsonRepresentation record, JnEmailSuppressionTarget target) {
		String email = record.getAsString(JnEntityEmailSuppressed.Fields.email);
		String lowerCaseEmail = email.toLowerCase();
		String normalizedEmail = lowerCaseEmail.trim();
		Long created = record.getAsLongNumber(JnEntityEmailSuppressed.Fields.created);
		String listName = target.suppressionList.name();
		CcpJsonRepresentation suppression = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityEmailSuppressed.Fields.email, normalizedEmail)
				.put(JnEntityEmailSuppressed.Fields.created, created)
				.put(JnEntityEmailSuppressed.Fields.suppressionList, listName);
		CcpJsonRepresentation suppressionWithReason = this.copyWhenFilled(record, suppression, JnEntityEmailSuppressed.Fields.reason);
		CcpJsonRepresentation suppressionWithStatus = this.copyWhenFilled(record, suppressionWithReason, JnEntityEmailSuppressed.Fields.status);
		CcpJsonRepresentation suppressionWithIp = this.copyWhenFilled(record, suppressionWithStatus, JnEntityEmailSuppressed.Fields.ip);
		return suppressionWithIp;
	}

	/**
	 * Copies an optional field, unless the provider left it empty.
	 * @param record the record of the provider
	 * @param suppression the record to write
	 * @param field the field
	 * @return the record to write, with the field when it was filled
	 */
	private CcpJsonRepresentation copyWhenFilled(CcpJsonRepresentation record, CcpJsonRepresentation suppression, CcpJsonFieldName field) {
		String value = record.getAsString(field);
		String trimmedValue = value.trim();
		boolean isEmpty = trimmedValue.isEmpty();

		if(isEmpty) {
			return suppression;
		}
		CcpJsonRepresentation suppressionWithField = suppression.put(field, trimmedValue);
		return suppressionWithField;
	}
}
