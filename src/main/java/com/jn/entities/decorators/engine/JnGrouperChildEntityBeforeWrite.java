package com.jn.entities.decorators.engine;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;

/**
 * Decorator that takes the record out of its groups before the writes, while it can still be read as it is stored. It
 * stays in the outer part of the chain (priority 7). The {@code after} side is the responsibility of
 * {@code JnGrouperChildEntityAfterWrite}.
 */
public class JnGrouperChildEntityBeforeWrite extends JnGrouperChildEntityOnWrite {

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildEntityBeforeWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity, annotation);
	}

	/**
	 * Takes the record out of the groups it is in, on either side (the save writes on its side and deletes from the other
	 * one); the groups of the new version come in afterwards.
	 * @param json the record
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		this.removeFromGroupsWhenStored(false, json);
		this.removeFromGroupsWhenStored(true, json);
		boolean inserted = this.entity.save(json);
		return inserted;
	}

	/**
	 * In an entity without twin the delete is for good: the record leaves its groups while it can still be read. In a twin
	 * entity the record only changes sides, which is handled after the delete.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean delete(CcpJsonRepresentation json) {
		this.removeFromGroupsWhenDeletedForGood(json);
		boolean deleted = this.entity.delete(json);
		return deleted;
	}

	/**
	 * Takes the record out of the groups of both sides, while it can still be read.
	 * @param json the record
	 * @return whether the record existed
	 */
	public boolean deleteAnyWhere(CcpJsonRepresentation json) {
		this.removeFromGroupsWhenStored(false, json);
		this.removeFromGroupsWhenStored(true, json);
		boolean deleted = this.entity.deleteAnyWhere(json);
		return deleted;
	}

	/**
	 * Takes the record out of the main groups when the delete is for good (an entity without twin).
	 * @param json the record
	 */
	private void removeFromGroupsWhenDeletedForGood(CcpJsonRepresentation json) {
		boolean onlyChangesSides = this.hasTwin();

		if(onlyChangesSides) {
			return;
		}

		this.removeFromGroupsWhenStored(false, json);
	}
}
