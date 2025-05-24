package com.jn.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.especifications.mensageria.receiver.CcpTopic;

public class JnDeleteKeysFromCache implements  CcpTopic, Consumer<String[]> {

	public static final JnDeleteKeysFromCache INSTANCE = new JnDeleteKeysFromCache();
	private static final String KEYS_TO_DELETE_IN_CACHE = "keysToDeleteInCache";
	
	private JnDeleteKeysFromCache() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		Collection<String> allCacheKeys = json.getAsStringList(KEYS_TO_DELETE_IN_CACHE);
		
		for (String cacheKey : allCacheKeys) {
			CcpCacheDecorator cache = new CcpCacheDecorator(cacheKey);
			cache.delete();
		}
		return json;
	}

	public void accept(String[] keysToDeleteInCache) {
		List<String> asList = Arrays.asList(keysToDeleteInCache);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(KEYS_TO_DELETE_IN_CACHE,asList);
		this.apply(json);
		
	}

}
