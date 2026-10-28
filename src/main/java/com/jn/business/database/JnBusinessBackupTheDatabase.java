package com.jn.business.database;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.json.defaultvalues.annotations.CcpJsonFieldDefaultValue;
import com.ccp.json.validations.fields.annotations.CcpJsonFieldValidatorArray;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Backs up the entities of the cost centers of the platform ({@code jn}, {@code js}, {@code vis} and {@code jb}) into a
 * bucket, through {@code CcpDbRequester.backup}: the database writes the backup straight to the bucket. The legacy
 * indexes, whose names have no prefix of a cost center, stay out of it.
 * <p>
 * Input: {@code bucketName} ({@code jobsnow-backup} when absent) and {@code entityNamePatterns} (the four cost centers
 * when absent). Output: the input plus {@code backup}, the details the database gave of it.
 */
public class JnBusinessBackupTheDatabase implements CcpBusiness {

	/** The input and the output of this business, with the validation rules of the input. */
	public static enum Fields implements CcpJsonFieldName {
		/** The bucket that receives the backup; {@code jobsnow-backup} when not given. */
		@CcpJsonFieldTypeString(minLength = 3, maxLength = 63)
		@CcpJsonFieldDefaultValue(defaultStrings = "jobsnow-backup")
		bucketName,

		/** The names of the entities in the backup, {@code *} standing for any text; the four cost centers when not given. */
		@CcpJsonFieldValidatorArray
		@CcpJsonFieldTypeString(minLength = 2, maxLength = 100)
		@CcpJsonFieldDefaultValue(defaultStrings = {"jn_*", "js_*", "vis_*", "jb_*"})
		entityNamePatterns,

		/** The details of the backup. */
		backup,
	}

	/** The single instance: the business keeps no state. */
	public static final JnBusinessBackupTheDatabase INSTANCE = new JnBusinessBackupTheDatabase();

	private JnBusinessBackupTheDatabase() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Orders the backup of the entities into the bucket.
	 * @param json the bucket and the names of the entities
	 * @return the input plus the details of the backup
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String bucketName = json.getAsString(Fields.bucketName);
		List<String> entityNamePatterns = json.getAsStringList(Fields.entityNamePatterns);
		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		CcpJsonRepresentation backup = dbRequester.backup(bucketName, entityNamePatterns);
		CcpJsonRepresentation jsonWithBackup = json.put(Fields.backup, backup);
		return jsonWithBackup;
	}
}
