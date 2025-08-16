package com.jn.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.especifications.mensageria.receiver.CcpTopic;
enum JnDeleteKeysFromCacheConstats  implements CcpJsonFieldName{
	keysToDeleteInCache
	
}
public class JnDeleteKeysFromCache implements  CcpTopic, Consumer<String[]> {

	public static final JnDeleteKeysFromCache INSTANCE = new JnDeleteKeysFromCache();
	
	private JnDeleteKeysFromCache() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		Collection<String> allCacheKeys = json.getAsStringList(JnDeleteKeysFromCacheConstats.keysToDeleteInCache);
		
		for (String cacheKey : allCacheKeys) {
			CcpCacheDecorator cache = new CcpCacheDecorator(cacheKey);
			cache.delete();
		}
		return json;
	}

	public void accept(String[] keysToDeleteInCache) {
		List<String> asList = Arrays.asList(keysToDeleteInCache);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JnDeleteKeysFromCacheConstats.keysToDeleteInCache, asList);
		this.apply(json);
		
	}

}
