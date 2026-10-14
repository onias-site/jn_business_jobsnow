package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.decorators.annotations.JnEntityGrouperChild;

/**
 * Decorator that puts the record in its groups after the writes. It stays inside the field transformers (priority 5):
 * the save sees the record as written, with the fields its parents come from already calculated. The {@code before} side
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
	 * Saves and puts the record in the groups of its parents, on the side saved.
	 * @param json the record
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		boolean inserted = this.entity.save(json);
		boolean twinSide = this.isTwinSide();
		this.addToGroups(twinSide, json);
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
