package com.jn.entities.decorators.engine;

import java.util.List;
import java.util.Map;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityGrouperChild;

/**
 * Decorator of {@code @CcpEntityGrouperChild} (given as its {@code afterDecoratorClass}) that keeps in the groups of
 * {@code JnEntityGrouper} the value of the field {@code childField} instead of the id of the record, so the group is read
 * with no read of the grouped entity. It stays inside the field transformers (priority 5), so a calculated field is
 * already filled.
 * <ul>
 * <li>save: saves and then puts the value in the groups of the parents of the record, once each;</li>
 * <li>delete and deleteAnyWhere: the groups are left as they are, since other records may have the same value and
 * nothing tells it with no query.</li>
 * </ul>
 * The groups are always on the main side of the grouper, and the parents are oneToMany: no field keeps their ids. Its
 * {@code beforeDecoratorClass} is {@code JnGrouperChildFieldEntityBeforeWrite}, which does nothing.
 */
public class JnGrouperChildFieldEntityAfterWrite extends JnGrouperChildEntityOnWrite {

	/** The name of the field kept as the child. */
	private final String childField;

	/**
	 * Wraps the entity.
	 * @param entity the entity decorated so far
	 * @param annotation the grouping configured for the entity
	 */
	public JnGrouperChildFieldEntityAfterWrite(CcpEntity entity, CcpEntityGrouperChild annotation) {
		super(entity, annotation);
		this.childField = annotation.childField();
	}

	/**
	 * Saves the record and puts the value of its field in the groups of its parents of every kind.
	 * @param json the record, its fields already transformed
	 * @return whether the record was inserted
	 */
	public boolean save(CcpJsonRepresentation json) {
		boolean inserted = this.entity.save(json);
		List<JnEntityGrouperParents> allParents = this.getAllParents();
		for (JnEntityGrouperParents parents : allParents) {
			Map<String, CcpJsonRepresentation> recordParents = parents.getParents(json);
			this.addToGroups(false, json, parents, recordParents);
		}
		return inserted;
	}

	/**
	 * What the record keeps in its groups.
	 * @param mainEntity the main side of the entity
	 * @param record the record, its fields already transformed
	 * @return the value of the field
	 */
	protected String getChild(CcpEntity mainEntity, CcpJsonRepresentation record) {
		CcpFieldName childFieldName = new CcpFieldName(this.childField);
		String child = record.getAsString(childFieldName);
		return child;
	}
}
