package com.jn.business.database;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.json.defaultvalues.annotations.CcpJsonFieldDefaultValue;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Deletes from the bucket of the backups every backup but the most recent one that went well (since 2026-11-27), through
 * {@code CcpDbRequester.deleteTheBackupsBeforeTheLatest}: the bucket keeps a single backup. It runs every day in the job
 * {@code jobsnow-backup-diario}, right after {@link JnBusinessBackupTheDatabase}, so the backup kept is the one of the
 * day; when the backup of the day fails, the one kept is the latest that went well, and with none nothing is deleted.
 * <p>
 * Input: {@code bucketName} ({@code jobsnow-backup} when absent). Output: the input plus {@code backups}, the backup kept
 * ({@code latestBackup}) and the ones deleted ({@code deletedBackups}).
 */
public class JnBusinessKeepOnlyTheLatestBackup implements CcpBusiness {

	/** The input and the output of this business, with the validation rules of the input. */
	public static enum Fields implements CcpJsonFieldName {
		/** The bucket of the backups; {@code jobsnow-backup} when not given. */
		@CcpJsonFieldTypeString(minLength = 3, maxLength = 63)
		@CcpJsonFieldDefaultValue(defaultStrings = "jobsnow-backup")
		bucketName,

		/** The backup kept and the ones deleted. */
		backups,
	}

	/** The single instance: the business keeps no state. */
	public static final JnBusinessKeepOnlyTheLatestBackup INSTANCE = new JnBusinessKeepOnlyTheLatestBackup();

	private JnBusinessKeepOnlyTheLatestBackup() {}

	public Class<?> getJsonValidationClass() {
		return Fields.class;
	}

	/**
	 * Deletes the backups before the latest one of the bucket.
	 * @param json the bucket
	 * @return the input plus the backup kept and the ones deleted
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String bucketName = json.getAsString(Fields.bucketName);
		CcpDbRequester dbRequester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		CcpJsonRepresentation backups = dbRequester.deleteTheBackupsBeforeTheLatest(bucketName);
		CcpJsonRepresentation jsonWithBackups = json.put(Fields.backups, backups);
		return jsonWithBackups;
	}
}
