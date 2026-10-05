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
 * Deletes keys from the cache. It works both as a business (a JSON with the list of keys) and as a consumer of an array
 * of keys, used as the cache cleanup callback of the database operations.
 */
public class JnDeleteKeysFromCache implements  CcpBusiness, Consumer<String[]> {
		
	/** Fields read by the cleanup. */
	enum JsonFieldNames implements CcpJsonFieldName{
		/** The {@code keysToDeleteInCache} field. */
		keysToDeleteInCache
	}

	/** The single instance. */
	public static final JnDeleteKeysFromCache INSTANCE = new JnDeleteKeysFromCache();
	
	/** Singleton; use {@link #INSTANCE}. */
	private JnDeleteKeysFromCache() {}
	
	/**
	 * Deletes every key in a single round trip to the cache server. A loop that deleted one key at a time was replaced,
	 * because {@code CcpCrud.deleteKeysInCache} triggers this cleanup before every search, including the read-only ones, so a
	 * query touching nine entities became nine network calls.
	 * @param json {@code keysToDeleteInCache}: the keys to delete
	 * @return the same JSON
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		Collection<String> allCacheKeys = json.getAsStringList(JsonFieldNames.keysToDeleteInCache);

		CcpCacheDecorator.deleteAll(allCacheKeys);

		return json;
	}

	/**
	 * Deletes the given keys.
	 * @param keysToDeleteInCache the keys to delete
	 */
	public void accept(String[] keysToDeleteInCache) {
		List<String> asList = Arrays.asList(keysToDeleteInCache);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.keysToDeleteInCache, asList);
		this.execute(json);
		
	}

}
