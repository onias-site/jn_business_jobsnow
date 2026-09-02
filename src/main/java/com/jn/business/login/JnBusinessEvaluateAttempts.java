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
 * Avalia tentativas de autenticação (senha ou token) comparando o valor fornecido
 * pelo usuário com o armazenado no banco, via CcpPasswordHandler.matches. Se correto,
 * delega ao business de sucesso. Se incorreto, incrementa o contador de tentativas;
 * ao atingir 3 tentativas erradas, aciona o business de bloqueio e lança
 * CcpErrorFlowDisturb com o status de excesso de tentativas; antes disso, lança o
 * status de "tipo errado" com o número de tentativas atual.
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

	JnBusinessEvaluateAttempts(Builder b) {
		this.entityToGetTheAttempts             = b.entityToGetTheAttempts;
		this.entityToGetTheSecret               = b.entityToGetTheSecret;
		this.databaseFieldName                  = b.databaseFieldName;
		this.userFieldName                      = b.userFieldName;
		this.statusToReturnWhenExceedAttempts   = b.statusToReturnWhenExceedAttempts;
		this.statusToReturnWhenWrongType        = b.statusToReturnWhenWrongType;
		this.topicToCreateTheLockWhenExceedTries = b.topicToCreateTheLockWhenExceedTries;
		this.topicToRegisterSuccess             = b.topicToRegisterSuccess;
		this.fieldAttempsName                   = b.fieldAttempsName;
		this.fieldEmailName                     = b.fieldEmailName;
	}

	public static Builder builder() {
		Builder builder = new Builder();
		return builder;
	}



	/**
	 * Busca o segredo no banco, compara com o valor do usuário usando CcpPasswordHandler,
	 * e controla o fluxo de sucesso/bloqueio/tentativas.
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
		
		CcpPasswordHandler dependency = CcpDependencyInjection.getDependency(CcpPasswordHandler.class);
		
		boolean correctSecret = dependency.matches(secretFromUser, secretFromDatabase);
		
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
			CcpErrorFlowDisturb ccpErrorFlowDisturb = new CcpErrorFlowDisturb(toReturn, this.statusToReturnWhenExceedAttempts);
			throw ccpErrorFlowDisturb;
		}
		
		String email = json.getAsString(this.fieldEmailName);
		CcpJsonRepresentation put2 = CcpOtherConstants.EMPTY_JSON
				.put(this.fieldAttempsName, updatedAttempts);
				CcpJsonRepresentation put = put2
				.put(this.fieldEmailName, email)
				;
		this.entityToGetTheAttempts.save(put);
		CcpJsonFieldName[] returnedFields = new CcpJsonFieldName[] {
				this.fieldAttempsName
		};
		CcpJsonRepresentation put3 = toReturn.put(this.fieldAttempsName, updatedAttempts);
		CcpErrorFlowDisturb ccpErrorFlowDisturb2 = new CcpErrorFlowDisturb(put3, this.statusToReturnWhenWrongType, returnedFields);
		throw ccpErrorFlowDisturb2;
	}

	@SuppressWarnings("serial")
	private static class JnErrorSecretFromDatabaseIsEmpty extends RuntimeException {
	}

	@SuppressWarnings("serial")
	private static class JnErrorSecretFromUserIsEmpty extends RuntimeException {
	}
}
