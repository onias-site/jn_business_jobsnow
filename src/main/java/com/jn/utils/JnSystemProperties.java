package com.jn.utils;

import java.util.Arrays;
import java.util.List;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpPropertiesDecorator;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Typed access to the system properties named {@code application_properties}, read from the environment variables, the
 * class path or a file, in this order.
 */
public class JnSystemProperties {
	

	
	/** All the system properties. */
	public final CcpJsonRepresentation systemProperties;
	
	/** The single instance. */
	public static final JnSystemProperties INSTANCE = new JnSystemProperties();
	
	/** Reads the properties once. */
	private JnSystemProperties() {
		CcpStringDecorator ccpStringDecorator = new CcpStringDecorator("application_properties");
		CcpPropertiesDecorator propertiesFrom = ccpStringDecorator.propertiesFrom();
		this.systemProperties = propertiesFrom.environmentVariablesOrClassLoaderOrFile();
		
	}
	
	
	/**
	 * Returns the systems served by this installation.
	 * @return the {@code systems} property
	 */
	public List<String> systems(){
		List<String> response = this.systemProperties.getAsStringList(Fields.systems);
		return response;
	}

	/**
	 * Tells whether the system runs on a developer machine.
	 * @return the {@code localEnvironment} property
	 */
	public boolean localEnvironment() {
		boolean localEnvironment = this.systemProperties.getAsBoolean(Fields.localEnvironment);
		return localEnvironment;
	}
	
	/**
	 * Returns the URL of the instant messenger API.
	 * @return the {@code urlInstantMessengerKey} property
	 */
	public String urlInstantMessengerKey() {
		String response = this.systemProperties.getAsString(Fields.urlInstantMessengerKey);
		return response;
	}
	
	/**
	 * Returns the credential of the e-mail API.
	 * @return the {@code tokenEmailKey} property
	 */
	public String tokenEmailValue() {
		String response = this.systemProperties.getAsString(Fields.tokenEmailKey);
		return response;
	}
	
	/**
	 * Returns the URL of the e-mail API.
	 * @return the {@code urlEmailKey} property
	 */
	public String urlEmailValue() {
		String response = this.systemProperties.getAsString(Fields.urlEmailKey);
		return response;
	}
	
	/**
	 * Returns the language of the messages sent to the support team.
	 * @return the {@code supportLanguage} property
	 */
	public String supportLanguage() {
		String response = this.systemProperties.getAsString(Fields.supportLanguage);
		return response;
	}
	
	/**
	 * Returns the credential of the instant messenger API.
	 * @return the {@code tokenInstantMessengerKey} property
	 */
	public String tokenInstantMessengerKey() {
		String response = this.systemProperties.getAsString(Fields.tokenInstantMessengerKey);
		return response;
	}
	
	/**
	 * Returns the credential of the database.
	 * @return the {@code database.secret} property
	 */
	public String databaseSecret() {
		String response = this.systemProperties.getAsString(Fields.databaseSecret);
		return response;
	}
	
	/**
	 * Returns the address of the database.
	 * @return the {@code database.address} property
	 */
	public String databaseAddress() {
		String response = this.systemProperties.getAsString(Fields.databaseAddress);
		return response;
	}
	
	/**
	 * Returns a property by its field.
	 * @param <T> the type of the value
	 * @param field the property
	 * @return the value, or {@code null} when absent
	 */
	public <T> T getSystemProperty(CcpJsonFieldName field) {
		T response = this.systemProperties.getAsObject(field);
		return response;
	}

	/**
	 * Returns a nested property, following the path of fields.
	 * @param fields the path
	 * @return the value, or an empty text when absent
	 */
	public String getSystemInnerProperty(CcpJsonFieldName... fields) {
		String response = this.systemProperties.getValueFromPath("", fields);
		return response;
	}
	
	/**
	 * Returns a nested JSON property, following the path of names.
	 * @param fields the path
	 * @return the nested JSON
	 */
	public CcpJsonRepresentation getSystemInnerJson(String... fields) {
		CcpJsonFieldName[] fieldNames = Arrays.stream(fields)
				.map(f -> new CcpFieldName(f))
				.toArray(CcpJsonFieldName[]::new);
		CcpJsonRepresentation response = this.systemProperties.getInnerJsonFromPath(fieldNames);
		return response;
	}

	/**
	 * Returns a property by its name.
	 * @param <T> the type of the value
	 * @param field the property name
	 * @return the value, or {@code null} when absent
	 */
	public <T> T getSystemProperty(String field) {
		CcpFieldName ccpFieldName = new CcpFieldName(field);
		T response = this.systemProperties.getAsObject(ccpFieldName);
		return response;
	}


	/**
	 * Returns how many wrong secrets lock a password or token.
	 * @return the {@code maxAttempts} property, 3 by default
	 */
	public int maxAttempts() {
		Integer asIntegerNumber = this.systemProperties.getOrDefault(Fields.maxAttempts, () -> 3);
		return asIntegerNumber;
	}
}
