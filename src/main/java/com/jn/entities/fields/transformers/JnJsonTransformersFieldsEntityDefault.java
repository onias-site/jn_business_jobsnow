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

/**
 * Conjunto de transformadores de campos padrão aplicados às entidades do JobsNow. Cada valor aplica
 * uma transformação específica: {@code email} valida e calcula hash SHA-1; {@code password} aplica
 * BCrypt; {@code token} gera token aleatório e aplica BCrypt; {@code timestamp} adiciona data/hora;
 * {@code tokenHash} calcula hash SHA-1 do token de sessão.
 */
public enum JnJsonTransformersFieldsEntityDefault implements CcpJsonTransformersDefaultEntityField, CcpJsonFieldName {
	email(true) {

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpJsonFieldName oldField = JnJsonCommonsFields.email;
			CcpJsonFieldName newField = JsonFieldNames.originalEmail;
			String value = json.getAsString(oldField);
			CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(value);
			CcpEmailDecorator email = ccpStringDecorator.email();
			boolean valid = email.isValid();

			boolean isNotAnEmail = false == valid;
			
			if(isNotAnEmail) {
				JnErrorIsNotAnEmail jnErrorIsNotAnEmail = new JnErrorIsNotAnEmail(value, json);
				throw jnErrorIsNotAnEmail;
			}
			
			CcpHashDecorator hash2 = email.hash();
			String hash = hash2.asString(CcpHashAlgorithm.SHA1);
			CcpJsonRepresentation put2 = json.put(oldField, hash);
			CcpJsonRepresentation put = put2.put(newField, value);
			return put;
		}
	},
	password(false) {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			boolean passwordAlreadyCalculated = json.containsAllFields(JsonFieldNames.passwordAlreadyCalculated);
			
			if(passwordAlreadyCalculated) {
				return json;
			}
			
			String token = json.getAsString(JnJsonCommonsFields.password);
			
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String passwordHash = dependency.getHash(token); 
			CcpJsonRepresentation put3 = json.put(JnJsonCommonsFields.password, passwordHash);

			CcpJsonRepresentation put = put3
					.put(JsonFieldNames.passwordAlreadyCalculated, true)
					;
			return put;
		}
	},
	token(false) {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

			String originalToken = json.getOrDefault(JsonFieldNames.originalToken, () -> super.getOriginalToken());
			 
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String token = dependency.getHash(originalToken);
			CcpJsonRepresentation put4 = json
					.put(JnEntityLoginToken.Fields.token, token);

					CcpJsonRepresentation put = put4
					.put(JsonFieldNames.originalToken, originalToken)
					;
			
			return put;
		}

	},
	timestamp(true) {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String tIMESTAMPName = CcpEntityField.TIMESTAMP.name();
			CcpFieldName ccpFieldName = new CcpFieldName(tIMESTAMPName);
		
			boolean containsAllFields = json.containsAllFields(ccpFieldName);
			
			if(containsAllFields) {
				return json;
			}

			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = ctd.getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format);
			CcpJsonRepresentation put5 = json.put(CcpEntityField.TIMESTAMP, ctd.content);

			CcpJsonRepresentation put = put5
					.put(CcpEntityField.DATE, formattedDateTime);
			
			return put;
		}

	},
	
	tokenHash(true){

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			String originalToken = json.getOrDefault(JsonFieldNames.token, () -> super.getOriginalToken());
			CcpStringDecorator ccpStringDecorator2 = new CcpStringDecorator(originalToken);
			CcpHashDecorator hash = ccpStringDecorator2.hash();
			
			String token = hash.asString(CcpHashAlgorithm.SHA1);
			CcpJsonRepresentation put6 = json
					.put(JnEntityLoginToken.Fields.token, token);

					CcpJsonRepresentation put = put6
					.put(JsonFieldNames.originalToken, originalToken)
					;
			
			return put;
		}}
	;
	
	
	private JnJsonTransformersFieldsEntityDefault(boolean canBePrimaryKey) {
			this.canBePrimaryKey = canBePrimaryKey;
		}

	private final boolean canBePrimaryKey;
	
	
	public static String getOriginalToken() {
		CcpTextDecorator lETTERS_AND_NUMBERSText = CcpOtherConstants.LETTERS_AND_NUMBERS.text();
		CcpTextDecorator generateToken = lETTERS_AND_NUMBERSText.generateToken(8);
		String originalToken = generateToken.content;
		return originalToken;
	}
	public static enum JsonFieldNames implements CcpJsonFieldName{
		originalEmail, originalToken, token, passwordAlreadyCalculated, tokenHash, originalMessage, messageHash
	}
	public boolean canBePrimaryKey() {
		return canBePrimaryKey;
	}

}
