package com.jn.utils;

import java.util.function.Consumer;

/**
 * Cache cleanup callback that deletes nothing, for the entities that are not cached. The groups of {@code JnEntityGrouper}
 * use it: each save of a resume writes hundreds of groups, and until 2026-10-14 every write deleted the cache key of each
 * one, a key that never existed (the grouper has no {@code @CcpEntityCache}), with the cost of one call to the cache per
 * group.
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
