package com.jn.entities.fields.counters;

import java.util.List;
import java.util.function.Function;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The key of the counter of records by month of insertion ({@code counterKeyClass} of a
 * {@code @CcpEntityGrouperChildCounter} counted by {@code JnGrouperChildCurrentCounterEntityBeforeWrite}): the year and
 * month ({@code yyyyMM}) of the {@code insertionDate} of the stored record, kept by {@code @CcpEntityInsertionDate}; no
 * key when the record has none.
 */
public class JnInsertionYearMonthCounterKeys implements Function<CcpJsonRepresentation, List<String>>{
	/** The single instance: the class keeps no state. */
	public final static JnInsertionYearMonthCounterKeys INSTANCE = new JnInsertionYearMonthCounterKeys();

	private JnInsertionYearMonthCounterKeys() {}

	/** The formats read and written. */
	enum Formats implements CcpJsonFieldName{
		INSERTION_DATE {
			public String getValue() {
				return "dd/MM/yyyy HH:mm:ss";
			}
		},
		YEAR_MONTH {
			public String getValue() {
				return "yyyyMM";
			}
		}
	}

	/**
	 * The year and month of the insertion of the record.
	 * @param json the record as stored
	 * @return the only key, {@code yyyyMM}, or none
	 */
	public List<String> apply(CcpJsonRepresentation json) {
		boolean noInsertionDate = false == json.containsField(JnJsonCommonsFields.insertionDate);

		if(noInsertionDate) {
			return List.of();
		}

		String insertionDate = json.getAsString(JnJsonCommonsFields.insertionDate);
		String insertionDateFormat = Formats.INSERTION_DATE.getValue();
		CcpTimeDecorator insertionTime = new CcpTimeDecorator(insertionDate, insertionDateFormat);
		String yearMonthFormat = Formats.YEAR_MONTH.getValue();
		String yearMonth = insertionTime.getFormattedDateTime(yearMonthFormat);
		return List.of(yearMonth);
	}
}
