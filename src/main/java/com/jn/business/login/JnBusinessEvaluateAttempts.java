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

	private JnBusinessEvaluateAttempts(Builder b) {
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
		return new Builder();
	}

	public static class Builder {
		private CcpEntity entityToGetTheAttempts;
		private CcpEntity entityToGetTheSecret;
		private CcpJsonFieldName databaseFieldName;
		private CcpJsonFieldName userFieldName;
		private CcpProcessStatus statusToReturnWhenExceedAttempts;
		private CcpProcessStatus statusToReturnWhenWrongType;
		private CcpBusiness topicToCreateTheLockWhenExceedTries;
		private CcpBusiness topicToRegisterSuccess;
		private CcpJsonFieldName fieldAttempsName;
		private CcpJsonFieldName fieldEmailName; 

		public Builder entityToGetTheAttempts(CcpEntity entity) {
			this.entityToGetTheAttempts = entity;
			return this;
		}
		public Builder entityToGetTheSecret(CcpEntity entity) {
			this.entityToGetTheSecret = entity;
			return this;
		}
		public Builder databaseFieldName(CcpJsonFieldName field) {
			this.databaseFieldName = field;
			return this;
		}
		public Builder userFieldName(CcpJsonFieldName field) {
			this.userFieldName = field;
			return this;
		}
		public Builder statusWhenExceedAttempts(CcpProcessStatus status) {
			this.statusToReturnWhenExceedAttempts = status;
			return this;
		}
		public Builder statusWhenWrongType(CcpProcessStatus status) {
			this.statusToReturnWhenWrongType = status;
			return this;
		}
		public Builder lockUsing(CcpBusiness business) {
			this.topicToCreateTheLockWhenExceedTries = business;
			return this;
		}
		public Builder onSuccess(CcpBusiness business) {
			this.topicToRegisterSuccess = business;
			return this;
		}
		public Builder attemptsFieldName(CcpJsonFieldName field) {
			this.fieldAttempsName = field;
			return this;
		}
		public Builder emailFieldName(CcpJsonFieldName field) {
			this.fieldEmailName = field;
			return this;
		}
		public JnBusinessEvaluateAttempts build() {
			return new JnBusinessEvaluateAttempts(this);
		}
	}

	/**
	 * Busca o segredo no banco, compara com o valor do usuário usando CcpPasswordHandler,
	 * e controla o fluxo de sucesso/bloqueio/tentativas.
	 */
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
		
		int maxAttempts = JnSystemProperties.INSTANCE.maxAttempts();
		double updatedAttempts = attemptsFromDatabase + 1;
		boolean exceededAttempts = updatedAttempts >= maxAttempts;
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
	 
	
	public String toString() {
		return "teste";
	}
	
	
}
