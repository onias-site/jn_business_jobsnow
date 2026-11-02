package com.jn.utils;

import java.util.function.Consumer;

/**
 * Cache cleanup callback that deletes nothing, for the writes that cannot leave a stale record in the cache: the load of
 * the Telegram positions only creates records (a record already there is not rewritten).
 */
public class JnDoNotDeleteKeysFromCache implements Consumer<String[]> {

	/** The single instance. */
	public static final JnDoNotDeleteKeysFromCache INSTANCE = new JnDoNotDeleteKeysFromCache();

	/** Singleton; use {@link #INSTANCE}. */
	private JnDoNotDeleteKeysFromCache() {}

	/**
	 * Does nothing.
	 * @param keysToDeleteInCache ignored
	 */
	public void accept(String[] keysToDeleteInCache) {
	}
}
