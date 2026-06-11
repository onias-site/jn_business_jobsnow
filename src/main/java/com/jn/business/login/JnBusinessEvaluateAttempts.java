package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;

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

	public JnBusinessEvaluateAttempts(
			CcpEntity entityToGetTheAttempts, 
			CcpEntity entityToGetTheSecret, 
			CcpJsonFieldName databaseFieldName, 
			CcpJsonFieldName userFieldName, 
			CcpProcessStatus statusToReturnWhenExceedAttempts, 
			CcpProcessStatus statusToReturnWhenWrongType,
			CcpBusiness topicToCreateTheLockWhenExceedTries,
			CcpBusiness topicToRegisterSuccess,
			CcpJsonFieldName fieldAttempsName,
			CcpJsonFieldName fieldEmailName
			) { 

		this.statusToReturnWhenExceedAttempts = statusToReturnWhenExceedAttempts;
		this.statusToReturnWhenWrongType = statusToReturnWhenWrongType;
		this.topicToRegisterSuccess = topicToRegisterSuccess;
		this.entityToGetTheAttempts = entityToGetTheAttempts;
		this.entityToGetTheSecret = entityToGetTheSecret;
		this.databaseFieldName = databaseFieldName;
		this.userFieldName = userFieldName;
		this.topicToCreateTheLockWhenExceedTries = topicToCreateTheLockWhenExceedTries;
		this.fieldAttempsName = fieldAttempsName;
		this.fieldEmailName = fieldEmailName;
				 
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		 
		String secretFromDatabase = json.getValueFromPath("",CcpEntity.JsonFieldNames._entities, this.entityToGetTheSecret, this.databaseFieldName);
		
		if(secretFromDatabase.trim().isEmpty()) {
			throw new RuntimeException();
		}
		
		String secretFromUser = json.getAsString(this.userFieldName);

		if(secretFromUser.trim().isEmpty()) {
			throw new RuntimeException();
		}
		
		CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		
		boolean correctSecret = dependency.matches(secretFromUser, secretFromDatabase);
		
		CcpJsonRepresentation toReturn = json.removeFields(JsonFieldNames.entities);
		
		if(correctSecret) {
			this.topicToRegisterSuccess.apply(toReturn); 
			return toReturn;
		}

		Double attemptsFromDatabase = json.getValueFromPath(0d, CcpEntity.JsonFieldNames._entities, this.entityToGetTheAttempts, this.fieldAttempsName);
		//LATER PARAMETRIZAR O 3
		double updatedAttempts = attemptsFromDatabase + 1;
		boolean exceededAttempts = updatedAttempts >= 3;
		if(exceededAttempts) {
			this.topicToCreateTheLockWhenExceedTries.apply(toReturn);
			throw new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenExceedAttempts);
		}
		
		String email = json.getAsString(this.fieldEmailName);
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(this.fieldAttempsName, updatedAttempts)
				.put(this.fieldEmailName, email)
				;
		this.entityToGetTheAttempts.save(put);
		CcpJsonFieldName[] returnedFields = new CcpJsonFieldName[] {
				this.fieldAttempsName
		};
		throw new CcpErrorFlowDisturb(toReturn.put(this.fieldAttempsName, updatedAttempts), this.statusToReturnWhenWrongType, returnedFields);
	}
	
	
	
}
