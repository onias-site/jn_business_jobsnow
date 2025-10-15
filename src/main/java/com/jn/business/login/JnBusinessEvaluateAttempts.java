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

	private final String userFieldName;
	
	private final String databaseFieldName;

	private final CcpProcessStatus statusToReturnWhenWrongType;
	
	private final CcpProcessStatus statusToReturnWhenExceedAttempts;
	
	private final CcpBusiness topicToRegisterSuccess;

	private final CcpBusiness topicToCreateTheLockWhenExceedTries;
	
	private final String fieldAttempsName;
	
	private final String fieldEmailName;

	public JnBusinessEvaluateAttempts(
			CcpEntity entityToGetTheAttempts, 
			CcpEntity entityToGetTheSecret, 
			String databaseFieldName, 
			String userFieldName, 
			CcpProcessStatus statusToReturnWhenExceedAttempts, 
			CcpProcessStatus statusToReturnWhenWrongType,
			CcpBusiness topicToCreateTheLockWhenExceedTries,
			CcpBusiness topicToRegisterSuccess,
			String fieldAttempsName,
			String fieldEmailName
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
		
		String secretFromDatabase = json.getDynamicVersion().getValueFromPath("","_entities", this.entityToGetTheSecret.getEntityName(), this.databaseFieldName);
		
		String secretFomUser = json.getDynamicVersion().getAsString(this.userFieldName);
		
		CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		
		boolean correctSecret = dependency.matches(secretFomUser, secretFromDatabase);
		
		CcpJsonRepresentation toReturn = json.removeField(JsonFieldNames.entities);
		
		if(correctSecret) {
			this.topicToRegisterSuccess.apply(toReturn); 
			return toReturn;
		}

		String attemptsEntityName = this.entityToGetTheAttempts.getEntityName();
		Double attemptsFromDatabase = json.getDynamicVersion().getValueFromPath(0d,"_entities", attemptsEntityName, this.fieldAttempsName);
		//LATER PARAMETRIZAR O 3
		boolean exceededAttempts = attemptsFromDatabase >= 3;
		if(exceededAttempts) {
			this.topicToCreateTheLockWhenExceedTries.apply(toReturn);
			throw new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenExceedAttempts);
		}
		
		String email = json.getDynamicVersion().getAsString(this.fieldEmailName);
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.getDynamicVersion().put(this.fieldAttempsName, attemptsFromDatabase + 1)
				.getDynamicVersion().put(this.fieldEmailName, email)
				;
		this.entityToGetTheAttempts.save(put);
		throw new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenWrongType);
	}
	
	
	
}
