package com.jn.json.transformers;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTextDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpEntityField;
import com.ccp.especifications.db.utils.decorators.engine.CcpEntityExpurgableOptions;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.utils.CcpHashAlgorithm;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;
import com.jn.exceptions.JnErrorIsNotAnEmail;
public enum JnJsonTransformersDefaultEntityFields implements Function<CcpJsonRepresentation, CcpJsonRepresentation> {
	email(true) {

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			JsonFieldNames oldField = JsonFieldNames.email;
			JsonFieldNames newField = JsonFieldNames.originalEmail;
			String value = json.getAsString(oldField);
			CcpEmailDecorator email = new CcpStringDecorator(value).email();
			
			boolean isNotAnEmail = email.isValid() == false;
			
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

	
	
	private JnJsonTransformersDefaultEntityFields(boolean canBePrimaryKey) {
			this.canBePrimaryKey = canBePrimaryKey;
		}

	private final boolean canBePrimaryKey;
	
	public static Function<CcpJsonRepresentation, CcpJsonRepresentation> getTransformer(CcpEntityField field){
		 
		Optional<JnJsonTransformersDefaultEntityFields> findFirst = Arrays.asList(JnJsonTransformersDefaultEntityFields.values()).stream().filter(x -> x.name().equals(field.name())).findFirst();
		 
		 boolean notFound = findFirst.isPresent() == false;

		 if(notFound) {
			 return CcpOtherConstants.DO_NOTHING;
		 }
		 
		 JnJsonTransformersDefaultEntityFields jnDefaultEntityFields = findFirst.get();

		 boolean isNotPrimaryKeyField = field.isPrimaryKey() == false;

		 if(isNotPrimaryKeyField) {
			 return jnDefaultEntityFields;
		 }
		 
		 if(jnDefaultEntityFields.canBePrimaryKey) {
			 return jnDefaultEntityFields;
		 }

		 throw new RuntimeException("The field '" + jnDefaultEntityFields.name() +"' can not be a primary key");
	}
	
	private String getOriginalToken() {
		CcpStringDecorator csd = new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		CcpTextDecorator text = csd.text();
		CcpTextDecorator generateToken = text.generateToken(8);
		String originalToken = generateToken.content;
		return originalToken;
	}
	enum JsonFieldNames implements CcpJsonFieldName{
		originalEmail, originalToken, email
	}

}
