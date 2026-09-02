package com.jn.business.login;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatus;

public class Builder { 
	CcpEntity entityToGetTheAttempts;
	CcpEntity entityToGetTheSecret;
	CcpJsonFieldName databaseFieldName;
	CcpJsonFieldName userFieldName;
	CcpProcessStatus statusToReturnWhenExceedAttempts;
	CcpProcessStatus statusToReturnWhenWrongType;
	CcpBusiness topicToCreateTheLockWhenExceedTries;
	CcpBusiness topicToRegisterSuccess;
	CcpJsonFieldName fieldAttempsName;
	CcpJsonFieldName fieldEmailName; 

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
		JnBusinessEvaluateAttempts jnBusinessEvaluateAttempts = new JnBusinessEvaluateAttempts(this);
		return jnBusinessEvaluateAttempts;
	}
}
