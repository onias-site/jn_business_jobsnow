package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;
import com.jn.utils.JnSystemProperties;

/**
 * Evaluates authentication attempts (password or token) by comparing the value supplied
 * by the user with the one stored in the database, via CcpPasswordHandler.matches. If it
 * matches, delegates to the success business. Otherwise, increments the attempt counter;
 * after 3 wrong attempts, triggers the lock business and throws CcpErrorFlowDisturb with
 * the "exceeded attempts" status; before that, throws the "wrong type" status with the
 * current number of attempts.
 */
public class JnBusinessEvaluateAttempts implements CcpBusiness{ 
	enum JsonFieldNames implements CcpJsonFieldName{
		entities
	}

	private final CcpEntity entityToGetTheSecret;
	 
	private final CcpEntity entityToGetTheAttempts;

	private final CcpJsonFieldName userFieldName;
	
	private final CcpJsonFieldName databaseFieldName;

	private final CcpProcessStatus statusToReturnWhenWrongType;
	
	private final CcpProcessStatus statusToReturnWhenExceedAttempts;
	
	private final CcpBusiness topicToRegisterSuccess;

	private final CcpBusiness topicToCreateTheLockWhenExceedTries;
	
	private final CcpJsonFieldName fieldAttempsName;
	
	private final CcpJsonFieldName fieldEmailName;

	JnBusinessEvaluateAttempts(Builder builder) {
		this.entityToGetTheAttempts             = builder.entityToGetTheAttempts;
		this.entityToGetTheSecret               = builder.entityToGetTheSecret;
		this.databaseFieldName                  = builder.databaseFieldName;
		this.userFieldName                      = builder.userFieldName;
		this.statusToReturnWhenExceedAttempts   = builder.statusToReturnWhenExceedAttempts;
		this.statusToReturnWhenWrongType        = builder.statusToReturnWhenWrongType;
		this.topicToCreateTheLockWhenExceedTries = builder.topicToCreateTheLockWhenExceedTries;
		this.topicToRegisterSuccess             = builder.topicToRegisterSuccess;
		this.fieldAttempsName                   = builder.fieldAttempsName;
		this.fieldEmailName                     = builder.fieldEmailName;
	}

	public static Builder builder() {
		Builder builder = new Builder();
		return builder;
	}



	/**
	 * Fetches the secret from the database, compares it with the user's value using
	 * CcpPasswordHandler, and drives the success/lock/attempts flow.
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String secretFromDatabase = json.getValueFromPath("",CcpEntity.JsonFieldNames._entities, this.entityToGetTheSecret, this.databaseFieldName);
		String secretFromDatabaseTrim = secretFromDatabase.trim();
		boolean secretFromDatabaseTrimEmpty = secretFromDatabaseTrim.isEmpty();

		if(secretFromDatabaseTrimEmpty) {
			JnErrorSecretFromDatabaseIsEmpty jnErrorSecretFromDatabaseIsEmpty = new JnErrorSecretFromDatabaseIsEmpty();
			throw jnErrorSecretFromDatabaseIsEmpty;
		}

		String secretFromUser = json.getAsString(this.userFieldName);
		String secretFromUserTrim = secretFromUser.trim();
		boolean secretFromUserTrimEmpty = secretFromUserTrim.isEmpty();

		if(secretFromUserTrimEmpty) {
			JnErrorSecretFromUserIsEmpty jnErrorSecretFromUserIsEmpty = new JnErrorSecretFromUserIsEmpty();
			throw jnErrorSecretFromUserIsEmpty;
		}
		
		CcpPasswordHandler passwordHandler = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);

		boolean correctSecret = passwordHandler.matches(secretFromUser, secretFromDatabase);
		
		CcpJsonRepresentation toReturn = json.removeFields(JsonFieldNames.entities);
		
		if(correctSecret) {
			this.topicToRegisterSuccess.execute(toReturn); 
			return toReturn;
		}

		Double attemptsFromDatabase = json.getValueFromPath(0d, CcpEntity.JsonFieldNames._entities, this.entityToGetTheAttempts, this.fieldAttempsName);
		
		int maxAttempts = JnSystemProperties.INSTANCE.maxAttempts();
		double updatedAttempts = attemptsFromDatabase + 1;
		boolean exceededAttempts = updatedAttempts >= maxAttempts;
		if(exceededAttempts) {
			this.topicToCreateTheLockWhenExceedTries.execute(toReturn);
			CcpErrorFlowDisturb exceededAttemptsError = new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenExceedAttempts);
			throw exceededAttemptsError;
		}

		String email = json.getAsString(this.fieldEmailName);
		CcpJsonRepresentation jsonWithAttempts = CcpOtherConstants.EMPTY_JSON
				.put(this.fieldAttempsName, updatedAttempts);
				CcpJsonRepresentation attemptsRecord = jsonWithAttempts
				.put(this.fieldEmailName, email)
				;
		this.entityToGetTheAttempts.save(attemptsRecord);
		CcpJsonFieldName[] returnedFields = new CcpJsonFieldName[] {
				this.fieldAttempsName
		};
		CcpJsonRepresentation jsonWithUpdatedAttempts = toReturn.put(this.fieldAttempsName, updatedAttempts);
		CcpErrorFlowDisturb wrongSecretError = new CcpErrorFlowDisturb(jsonWithUpdatedAttempts, this.statusToReturnWhenWrongType, returnedFields);
		throw wrongSecretError;
	}

	@SuppressWarnings("serial")
	private static class JnErrorSecretFromDatabaseIsEmpty extends RuntimeException {
	}

	@SuppressWarnings("serial")
	private static class JnErrorSecretFromUserIsEmpty extends RuntimeException {
	}
}
