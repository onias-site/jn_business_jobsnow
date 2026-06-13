package com.jn.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.business.CcpBusiness;
/**
 * Utilitário Singleton para exclusão de chaves do cache (GCP Memcache). Implementa tanto
 * {@code CcpBusiness} (aceita JSON com lista de chaves) quanto {@code Consumer<String[]>}
 * (aceita array de chaves), sendo usado como callback de limpeza de cache após operações bulk.
 */
public class JnDeleteKeysFromCache implements  CcpBusiness, Consumer<String[]> {
	//TODO JSON VALIDATIONS	
	enum JsonFieldNames implements CcpJsonFieldName{
		keysToDeleteInCache
	}

	public static final JnDeleteKeysFromCache INSTANCE = new JnDeleteKeysFromCache();
	
	private JnDeleteKeysFromCache() {}
	
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		
		Collection<String> allCacheKeys = json.getAsStringList(JsonFieldNames.keysToDeleteInCache);
		
		for (String cacheKey : allCacheKeys) {
			CcpCacheDecorator cache = new CcpCacheDecorator(cacheKey);
			cache.delete();
		}
		return json;
	}

	public void accept(String[] keysToDeleteInCache) {
		List<String> asList = Arrays.asList(keysToDeleteInCache);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.keysToDeleteInCache, asList);
		this.apply(json);
		
	}

}
