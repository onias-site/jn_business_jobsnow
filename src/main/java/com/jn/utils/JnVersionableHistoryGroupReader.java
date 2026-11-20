package com.jn.utils;

import java.util.List;
import java.util.function.Consumer;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Reads a group of the history found by {@code JnVersionableHistory.consumeVersions}: hands over each version written
 * since the given time, with the {@code parentJson} of the group ({@code entity}, {@code id} and {@code primaryKey}).
 */
class JnVersionableHistoryGroupReader implements Consumer<CcpJsonRepresentation> {

	/** The oldest {@code timestamp} handed over. */
	private final long since;

	/** Receives each version. */
	private final Consumer<CcpJsonRepresentation> versionConsumer;

	/**
	 * Keeps what each group needs.
	 * @param since the oldest {@code timestamp} handed over
	 * @param versionConsumer receives each version
	 */
	JnVersionableHistoryGroupReader(long since, Consumer<CcpJsonRepresentation> versionConsumer) {
		this.since = since;
		this.versionConsumer = versionConsumer;
	}

	/**
	 * Hands over the versions of the group written since the given time.
	 * @param group the group, with {@code parentJson} and {@code child}
	 */
	public void accept(CcpJsonRepresentation group) {
		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		List<CcpJsonRepresentation> versions = JnVersionableHistory.getVersions(group);
		for (CcpJsonRepresentation version : versions) {
			Long timestamp = version.getAsLongNumber(JnJsonCommonsFields.timestamp);
			boolean writtenBefore = timestamp < this.since;

			if(writtenBefore) {
				continue;
			}

			CcpJsonRepresentation versionWithItsRecord = parentJson.mergeWithAnotherJson(version);
			this.versionConsumer.accept(versionWithItsRecord);
		}
	}
}
