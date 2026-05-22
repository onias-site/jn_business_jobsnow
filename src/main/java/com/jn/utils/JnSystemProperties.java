package com.jn.utils;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpDynamicJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpPropertiesDecorator;
import com.ccp.decorators.CcpStringDecorator;

public class JnSystemProperties {
	
	public static enum Fields implements CcpJsonFieldName{
		databaseAddress{
			public String getValue() {
				return "database.address";
			}
		},
		databaseSecret{
			public String getValue() {
				return "database.secret";
			}
		},
		supportLanguage,
		urlEmailKey,
		urlInstantMessengerKey,
		tokenEmailKey,
		tokenInstantMessengerKey,
		localEnvironment,
		languages,
		systems
		
	}
	
	public final CcpJsonRepresentation systemProperties;
	
	public JnSystemProperties() {
		CcpStringDecorator ccpStringDecorator = new CcpStringDecorator("application_properties");
		CcpPropertiesDecorator propertiesFrom = ccpStringDecorator.propertiesFrom();
		this.systemProperties = propertiesFrom.environmentVariablesOrClassLoaderOrFile();
		
	}
	
	
	public List<String> systems(){
		List<String> response = this.systemProperties.getAsStringList(Fields.systems);
		return response;
	}
	public List<String> languages(){
		List<String> response = this.systemProperties.getAsStringList(Fields.languages);
		return response;
	}
	
	public boolean localEnvironment() {
		boolean localEnvironment = this.systemProperties.getAsBoolean(Fields.localEnvironment);
		return localEnvironment;
	}
	
	public String urlInstantMessengerKey() {
		String response = this.systemProperties.getAsString(Fields.urlInstantMessengerKey);
		return response;
	}
	
	public String tokenEmailValue() {
		String response = this.systemProperties.getAsString(Fields.tokenEmailKey);
		return response;
	}
	
	public String urlEmailValue() {
		String response = this.systemProperties.getAsString(Fields.urlEmailKey);
		return response;
	}
	
	public String supportLanguage() {
		String response = this.systemProperties.getAsString(Fields.supportLanguage);
		return response;
	}
	
	public String tokenInstantMessengerKey() {
		String response = this.systemProperties.getAsString(Fields.tokenInstantMessengerKey);
		return response;
	}
	
	public String databaseSecret() {
		String response = this.systemProperties.getAsString(Fields.databaseSecret);
		return response;
	}
	
	public String databaseAddress() {
		String response = this.systemProperties.getAsString(Fields.databaseAddress);
		return response;
	}
	
	public <T> T getSystemProperty(CcpJsonFieldName field) {
		T response = this.systemProperties.getAsObject(field);
		return response;
	}

	public String getSystemInnerProperty(CcpJsonFieldName... fields) {
		String response = this.systemProperties.getValueFromPath("", fields);
		return response;
	}
	
	public CcpJsonRepresentation getSystemInnerJson(String... fields) {
		CcpDynamicJsonRepresentation dynamicVersion = this.systemProperties.getDynamicVersion();
		CcpJsonRepresentation response = dynamicVersion.getInnerJsonFromPath(fields);
		return response;
	}
	
	public <T> T getSystemProperty(String field) {
		CcpDynamicJsonRepresentation dynamicVersion = this.systemProperties.getDynamicVersion();
		T response = dynamicVersion.getAsObject(field);
		return response;
	}
}
