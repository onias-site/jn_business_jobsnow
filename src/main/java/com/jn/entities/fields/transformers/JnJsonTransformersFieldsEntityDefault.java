package com.jn.entities.fields.transformers;


import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.hash.CcpHashAlgorithm;
import com.jn.entities.JnEntityLoginToken;
import com.jn.exceptions.JnErrorIsNotAnEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;

import com.ccp.json.fields.validation.CcpJsonCommonsFields;

/**
 * Set of default field transformers applied to the JobsNow entities. Each value applies a specific
 * transformation: {@code email} validates and computes a SHA-1 hash; {@code password} applies
 * BCrypt; {@code token} generates a random token and applies BCrypt; {@code timestamp} adds date/time;
 * {@code tokenHash} computes the SHA-1 hash of the session token.
 */
public enum JnJsonTransformersFieldsEntityDefault implements CcpJsonTransformersDefaultEntityField, CcpJsonFieldName {
	/** Validates the e-mail and replaces it with its SHA-1 hash, keeping the original in {@code originalEmail}. */
	email(true) {

		/**
		 * Transforms the e-mail.
		 * @param json the record
		 * @return the record with the hash and the original e-mail
		 * @throws JnErrorIsNotAnEmail when the value is not a valid e-mail
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonFieldName oldField = JnJsonCommonsFields.email;
			CcpJsonFieldName newField = JsonFieldNames.originalEmail;
			String value = json.getAsString(oldField);
			CcpStringDecorator valueDecorator = new CcpStringDecorator(value);
			CcpEmailDecorator emailDecorator = valueDecorator.email();
			boolean valid = emailDecorator.isValid();

			boolean isNotAnEmail = false == valid;
			
			if(isNotAnEmail) {
				JnErrorIsNotAnEmail jnErrorIsNotAnEmail = new JnErrorIsNotAnEmail(value, json);
				throw jnErrorIsNotAnEmail;
			}
			
			CcpHashDecorator emailHashDecorator = emailDecorator.hash();
			String hash = emailHashDecorator.asString(CcpHashAlgorithm.SHA1);
			CcpJsonRepresentation jsonWithHashedEmail = json.put(oldField, hash);
			CcpJsonRepresentation jsonWithOriginalEmail = jsonWithHashedEmail.put(newField, value);
			return jsonWithOriginalEmail;
		}
	},
	/** Replaces the password with its BCrypt hash, once ({@code passwordAlreadyCalculated} marks it as done). */
	password(false) {
		/**
		 * Transforms the password.
		 * @param json the record
		 * @return the record with the hash, or the input when it was already transformed
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			boolean passwordAlreadyCalculated = json.containsAllFields(JsonFieldNames.passwordAlreadyCalculated);
			
			if(passwordAlreadyCalculated) {
				return json;
			}
			
			String plainPassword = json.getAsString(JnJsonCommonsFields.password);
			
			CcpPasswordHandler passwordHandler = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String passwordHash = passwordHandler.getHash(plainPassword); 
			CcpJsonRepresentation jsonWithHashedPassword = json.put(JnJsonCommonsFields.password, passwordHash);

			CcpJsonRepresentation jsonWithPasswordFlag = jsonWithHashedPassword
					.put(JsonFieldNames.passwordAlreadyCalculated, true)
					;
			return jsonWithPasswordFlag;
		}
	},
	/**
	 * Replaces the login token with its BCrypt hash, keeping the original in {@code originalToken}; a new token is
	 * generated when {@code originalToken} is absent.
	 */
	token(false) {
		/**
		 * Transforms the token.
		 * @param json the record
		 * @return the record with the hash and the original token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			String originalToken = json.getOrDefault(JnJsonCommonsFields.originalToken, () -> super.getOriginalToken());
			 
			CcpPasswordHandler passwordHandler = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String hashedToken = passwordHandler.getHash(originalToken);
			CcpJsonRepresentation jsonWithHashedToken = json
					.put(JnEntityLoginToken.Fields.token, hashedToken);

					CcpJsonRepresentation jsonWithOriginalToken = jsonWithHashedToken
					.put(JnJsonCommonsFields.originalToken, originalToken)
					;
			
			return jsonWithOriginalToken;
		}

	},
	/** Adds the current {@code timestamp} and {@code date} (to the millisecond), unless there is a timestamp already. */
	timestamp(true) {
		/**
		 * Adds the current time.
		 * @param json the record
		 * @return the record with the time
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String timestampFieldName = CcpEntityField.TIMESTAMP.name();
			CcpFieldName timestampField = new CcpFieldName(timestampFieldName);
		
			boolean containsAllFields = json.containsAllFields(timestampField);
			
			if(containsAllFields) {
				return json;
			}

			CcpTimeDecorator now = new CcpTimeDecorator();
			String formattedDateTime = now.getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format);
			CcpJsonRepresentation jsonWithTimestamp = json.put(CcpEntityField.TIMESTAMP, now.content);

			CcpJsonRepresentation jsonWithDate = jsonWithTimestamp
					.put(CcpEntityField.DATE, formattedDateTime);
			
			return jsonWithDate;
		}

	},
	
	/**
	 * Replaces the {@code token} of the session with its SHA-1 hash, keeping the original in {@code originalToken}; a new
	 * token is generated when there is none.
	 */
	tokenHash(true){

		/**
		 * Transforms the token.
		 * @param json the record
		 * @return the record with the hash and the original token
		 */
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			String originalToken = json.getOrDefault(CcpJsonCommonsFields.token, () -> super.getOriginalToken());
			CcpStringDecorator originalTokenDecorator = new CcpStringDecorator(originalToken);
			CcpHashDecorator hash = originalTokenDecorator.hash();
			
			String tokenHashValue = hash.asString(CcpHashAlgorithm.SHA1);
			CcpJsonRepresentation jsonWithHashedToken = json
					.put(JnEntityLoginToken.Fields.token, tokenHashValue);

					CcpJsonRepresentation jsonWithOriginalToken = jsonWithHashedToken
					.put(JnJsonCommonsFields.originalToken, originalToken)
					;
			
			return jsonWithOriginalToken;
		}}
	;
	
	
	/**
	 * Associates the transformer with whether it can be part of a primary key.
	 * @param canBePrimaryKey whether the result can be part of a primary key
	 */
	private JnJsonTransformersFieldsEntityDefault(boolean canBePrimaryKey) {
			this.canBePrimaryKey = canBePrimaryKey;
		}

	/** Whether the result can be part of a primary key (BCrypt hashes can not: they change on each run). */
	private final boolean canBePrimaryKey;
	
	
	/**
	 * Generates a new random token of 8 letters and numbers.
	 * @return the token
	 */
	public static String getOriginalToken() {
		CcpTextDecorator lettersAndNumbers = CcpOtherConstants.LETTERS_AND_NUMBERS.text();
		CcpTextDecorator generatedToken = lettersAndNumbers.generateToken(8);
		String originalToken = generatedToken.content;
		return originalToken;
	}
	/** Fields written by the transformers. */
	public static enum JsonFieldNames implements CcpJsonFieldName{
		// originalToken and token remain declared because the test project references them
		/** The {@code originalEmail} field. */
		originalEmail,
		/** The {@code originalToken} field. */
		originalToken,
		/** The {@code token} field. */
		token,
		/** The {@code passwordAlreadyCalculated} field. */
		passwordAlreadyCalculated,
		/** The {@code tokenHash} field. */
		tokenHash,
		/** The {@code originalMessage} field. */
		originalMessage,
		/** The {@code messageHash} field. */
		messageHash
	}
	/**
	 * Tells whether the result can be part of a primary key.
	 * @return {@code true} for a deterministic transformer
	 */
	public boolean canBePrimaryKey() {
		return canBePrimaryKey;
	}

}
