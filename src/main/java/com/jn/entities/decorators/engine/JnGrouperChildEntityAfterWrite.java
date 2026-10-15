package com.jn.entities.decorators.engine;

import java.util.Map;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.decorators.annotations.JnEntityGrouperChild;

/**
 * Decorator that puts the record in its groups after the writes. It stays inside the field transformers (priority 5):
 * the save sees the record with the fields its parents come from already transformed, and it is the one that fills the
 * field of {@code @JnStoredParentIdsField} with the ids of those parents. The {@code before} side
 * is the responsibility of {@code JnGrouperChildEntityBeforeWrite}.
 */
public class JnGrouperChildEntityAfterWrite extends JnGrouperChildEntityOnWrite {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildEntityAfterWrite(CcpEntity entity, JnEntityGrouperChild annotation) {
		super(entity, annotation);
	}

	/**
	 * Calculates the parents of the record once: their ids fill the field annotated with {@code @JnStoredParentIdsField}
	 * before the save, and the record enters their groups, on the side saved, after it.
	 * @param json the record
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		JnEntityGrouperParents parents = this.getParents();
		Map<String, CcpJsonRepresentation> recordParents = parents.getParents(json);
		CcpJsonRepresentation jsonWithParentIds = parents.putParentIds(json, recordParents);
		boolean inserted = this.entity.save(jsonWithParentIds);
		boolean twinSide = this.isTwinSide();
		this.addToGroups(twinSide, jsonWithParentIds, parents, recordParents);
		return inserted;
	}

	/**
	 * Deletes and, in a twin entity, moves the record between the sides of the grouper as it moved between the sides of
	 * the entity: the deactivation (delete on the main side) leaves the main groups and enters the twin ones; the
	 * reactivation (delete on the twin side) does the opposite.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		boolean deleted = this.entity.delete(json);
		boolean nothingMoved = false == deleted || false == this.hasTwin();

		if(nothingMoved) {
			return deleted;
		}

		boolean fromTwinSide = this.isTwinSide();
		boolean toTwinSide = false == fromTwinSide;
		// the record is read where it went
		CcpEntity destination = this.getSideEntity(toTwinSide);
		CcpJsonRepresentation movedRecord = destination.getOneById(json);
		this.removeFromGroups(fromTwinSide, movedRecord);
		this.addToGroups(toTwinSide, movedRecord);
		return true;
	}
}
