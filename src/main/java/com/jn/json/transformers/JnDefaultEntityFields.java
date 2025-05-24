package com.jn.json.transformers;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.constantes.CcpStringConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpHashDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.jn.exceptions.JnIsNotAnEmail;

public enum JnDefaultEntityFields implements Function<CcpJsonRepresentation, CcpJsonRepresentation> {
	email {

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String oldField = CcpStringConstants.EMAIL.value;
			String newField = "originalEmail";
			String value = json.getAsString(oldField);
			CcpEmailDecorator email = new CcpStringDecorator(value).email();
			
			boolean isNotAnEmail = email.isValid() == false;
			
			if(isNotAnEmail) {
				throw new JnIsNotAnEmail(value, json);
			}
			
			CcpHashDecorator hash2 = email.hash();
			String hash = hash2.asString(CcpHashAlgorithm.SHA1);
			CcpJsonRepresentation put2 = json.put(oldField, hash);
			CcpJsonRepresentation put = put2.put(newField, value);
			return put;
		}
	},
	password {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String token = json.getAsString(JnEntityLoginPassword.Fields.password.name());
			
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String passwordHash = dependency.getHash(token); 
			
			CcpJsonRepresentation put = json.put(JnEntityLoginPassword.Fields.password.name(), passwordHash);
			
			return put;
		}

	},
	token {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			String createOriginalToken = super.getOriginalToken();
			String originalToken = json.getOrDefault("originalToken", createOriginalToken);
			 
			CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
			
			String token = dependency.getHash(originalToken);
		
			CcpJsonRepresentation put = json
					.put(JnEntityLoginToken.Fields.token.name(), token)
					.put("originalToken", originalToken)
					;
			
			return put;
		}

	},
	timestamp {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			CcpTimeDecorator ctd = new CcpTimeDecorator();
			String formattedDateTime = ctd.getFormattedDateTime(CcpEntityExpurgableOptions.millisecond.format);
			boolean containsAllFields = json.containsAllFields(CcpEntityField.TIMESTAMP.name());
			
			if(containsAllFields) {
				return json;
			}
			
			CcpJsonRepresentation put = json.put(CcpEntityField.TIMESTAMP.name(), ctd.content).put(CcpEntityField.DATE.name(), formattedDateTime);
			
			return put;
		}

	},
	
	tokenHash{

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			
			String originalToken = json.getOrDefault(JnEntityLoginSessionValidation.Fields.token.name(), super.getOriginalToken());
			CcpHashDecorator hash = new CcpStringDecorator(originalToken).hash();
			
			String token = hash.asString(CcpHashAlgorithm.SHA1);
		
			CcpJsonRepresentation put = json
					.put(JnEntityLoginToken.Fields.token.name(), token)
					.put("originalToken", originalToken)
					;
			
			return put;
		}}
	;

	
	public static Function<CcpJsonRepresentation, CcpJsonRepresentation> getTransformer(CcpEntityField field){
		 
		Optional<JnDefaultEntityFields> findFirst = Arrays.asList(JnDefaultEntityFields.values()).stream().filter(x -> x.name().equals(field.name())).findFirst();
		 
		 boolean notFound = findFirst.isPresent() == false;

		 if(notFound) {
			 return CcpOtherConstants.DO_NOTHING;
		 }
		 
		 JnDefaultEntityFields jnDefaultEntityFields = findFirst.get();
		
		 return jnDefaultEntityFields;
	}
	
	private String getOriginalToken() {
		CcpStringDecorator csd = new CcpStringDecorator(CcpStringConstants.CHARACTERS_TO_GENERATE_TOKEN.value);
		CcpTextDecorator text = csd.text();
		CcpTextDecorator generateToken = text.generateToken(8);
		String originalToken = generateToken.content;
		return originalToken;
	}

}
