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
	email(true) {

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
	password(false) {
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
	token(false) {
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
	timestamp(true) {
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
	
	tokenHash(true){

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
	
	
	private JnJsonTransformersFieldsEntityDefault(boolean canBePrimaryKey) {
			this.canBePrimaryKey = canBePrimaryKey;
		}

	private final boolean canBePrimaryKey;
	
	
	public static String getOriginalToken() {
		CcpTextDecorator lettersAndNumbers = CcpOtherConstants.LETTERS_AND_NUMBERS.text();
		CcpTextDecorator generatedToken = lettersAndNumbers.generateToken(8);
		String originalToken = generatedToken.content;
		return originalToken;
	}
	public static enum JsonFieldNames implements CcpJsonFieldName{
		// originalToken and token remain declared because the test project references them
		originalEmail, originalToken, token, passwordAlreadyCalculated, tokenHash, originalMessage, messageHash
	}
	public boolean canBePrimaryKey() {
		return canBePrimaryKey;
	}

}
