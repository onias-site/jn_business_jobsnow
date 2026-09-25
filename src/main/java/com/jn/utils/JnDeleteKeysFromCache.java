package com.jn.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
/**
 * Utilitário Singleton para exclusão de chaves do cache (GCP Memcache). Implementa tanto
 * {@code CcpBusiness} (aceita JSON com lista de chaves) quanto {@code Consumer<String[]>}
 * (aceita array de chaves), sendo usado como callback de limpeza de cache após operações bulk.
 */
public class JnDeleteKeysFromCache implements  CcpBusiness, Consumer<String[]> {
		
	enum JsonFieldNames implements CcpJsonFieldName{
		keysToDeleteInCache
	}

	public static final JnDeleteKeysFromCache INSTANCE = new JnDeleteKeysFromCache();
	
	private JnDeleteKeysFromCache() {}
	
	/**
	 * Apaga todas as chaves numa só conversa com o servidor de cache.
	 *
	 * <p>Antes daqui saía um laço que apagava chave por chave, e como
	 * {@code CcpCrud.deleteKeysInCache} dispara esta limpeza antes de <b>toda</b> busca — inclusive as
	 * de leitura pura — uma consulta que tocava nove entidades virava nove idas à rede.</p>
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		Collection<String> allCacheKeys = json.getAsStringList(JsonFieldNames.keysToDeleteInCache);

		CcpCacheDecorator.deleteAll(allCacheKeys);

		return json;
	}

	public void accept(String[] keysToDeleteInCache) {
		List<String> asList = Arrays.asList(keysToDeleteInCache);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.keysToDeleteInCache, asList);
		this.execute(json);
		
	}

}
