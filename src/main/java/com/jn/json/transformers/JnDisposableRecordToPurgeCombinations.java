package com.jn.json.transformers;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.entities.JnEntityVirtualDisposableRecordToPurge;

/**
 * The parent of a copy of {@code JnEntityDisposableRecord} among the records to purge
 * ({@link JnEntityVirtualDisposableRecordToPurge}): its own {@code entity} and {@code id} (oneToMany: a copy is the copy of
 * a single record).
 */
public class JnDisposableRecordToPurgeCombinations implements Function<CcpJsonRepresentation, List<CcpJsonRepresentation>> {

	/** The single instance. */
	public static final JnDisposableRecordToPurgeCombinations INSTANCE = new JnDisposableRecordToPurgeCombinations();

	private JnDisposableRecordToPurgeCombinations() {}

	/**
	 * The only combination: the {@code entity} and the {@code id} of the copy.
	 * @param json the copy, or the key of a copy
	 * @return the combination
	 */
	public List<CcpJsonRepresentation> apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation combination = json.getJsonPiece(JnEntityVirtualDisposableRecordToPurge.Fields.entity, JnEntityVirtualDisposableRecordToPurge.Fields.id);
		List<CcpJsonRepresentation> combinations = List.of(combination);
		return combinations;
	}
}
