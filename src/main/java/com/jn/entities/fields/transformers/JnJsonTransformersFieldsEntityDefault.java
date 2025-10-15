package com.jn.entities.fields.transformers;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.CcpJsonTransformersDefaultEntityField;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.utils.CcpHashAlgorithm;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.exceptions.JnErrorIsNotAnEmail;

public enum JnJsonTransformersFieldsEntityDefault implements CcpJsonTransformersDefaultEntityField {
	email(true) {

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JsonFieldNames oldField = JsonFieldNames.email;
			JsonFieldNames newField = JsonFieldNames.originalEmail;
			String value = json.getAsString(oldField);
			CcpEmailDecorator email = new CcpStringDecorator(value).email();
			
			boolean isNotAnEmail = false == email.isValid();
			
			if(isNotAnEmail) {
				throw new JnErrorIsNotAnEmail(value, json);
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
			String token = json.getAsString(JnEntityLoginPassword.Fields.password);
			
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String passwordHash = dependency.getHash(token); 
			
			CcpJsonRepresentation put = json.put(JnEntityLoginPassword.Fields.password, passwordHash);
			
			return put;
		}

	},
	token(false) {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String createOriginalToken = super.getOriginalToken();
			String originalToken = json.getOrDefault(JsonFieldNames.originalToken, createOriginalToken);
			 
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String token = dependency.getHash(originalToken);
		
			CcpJsonRepresentation put = json
					.put(JnEntityLoginToken.Fields.token, token)
					.put(JsonFieldNames.originalToken, originalToken)
					;
			
			return put;
		}

	},
	timestamp(true) {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = ctd.getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format);
			boolean containsAllFields = json.getDynamicVersion().containsAllFields(CcpEntityField.TIMESTAMP.name());
			
			if(containsAllFields) {
				return json;
			}
			
			CcpJsonRepresentation put = json.put(CcpEntityField.TIMESTAMP, ctd.content)
					.put(CcpEntityField.DATE, formattedDateTime);
			
			return put;
		}

	},
	
	tokenHash(true){

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			String originalToken = json.getOrDefault(JnEntityLoginSessionValidation.Fields.token, super.getOriginalToken());
			CcpHashDecorator hash = new CcpStringDecorator(originalToken).hash();
			
			String token = hash.asString(CcpHashAlgorithm.SHA1);
		
			CcpJsonRepresentation put = json
					.put(JnEntityLoginToken.Fields.token, token)
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
		CcpStringDecorator csd = new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		CcpTextDecorator text = csd.text();
		CcpTextDecorator generateToken = text.generateToken(8);
		String originalToken = generateToken.content;
		return originalToken;
	}
	enum JsonFieldNames implements CcpJsonFieldName{
		originalEmail, originalToken, email
	}
	public boolean canBePrimaryKey() {
		return canBePrimaryKey;
	}

}
