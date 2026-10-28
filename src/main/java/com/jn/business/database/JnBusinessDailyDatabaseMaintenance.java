package com.jn.business.database;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * The scheduled process that runs once a day ({@code CcpCronTasksController}, topic
 * {@code com.jn.business.database.JnBusinessDailyDatabaseMaintenance}): first the purge of the expired disposable records
 * ({@link JnBusinessPurgeExpiredDisposableRecords}), then the backup of the entities of the cost centers
 * ({@link JnBusinessBackupTheDatabase}), so the backup is taken without what deserved to be purged.
 * <p>
 * Input: the one of the backup ({@code bucketName} and {@code entityNamePatterns}, both optional). Output: the input plus
 * {@code purge} (the counters of the purge) and {@code backup} (the details of the backup).
 */
public class JnBusinessDailyDatabaseMaintenance implements CcpBusiness {

	/** The fields of the output. */
	public static enum Fields implements CcpJsonFieldName {
		/** The counters of the purge. */
		purge,
	}

	/** The single instance: the business keeps no state. */
	public static final JnBusinessDailyDatabaseMaintenance INSTANCE = new JnBusinessDailyDatabaseMaintenance();

	private JnBusinessDailyDatabaseMaintenance() {}

	/**
	 * Purges the expired records and then backs up the database.
	 * @param json the input of the backup
	 * @return the input plus the counters of the purge and the details of the backup
	 */
	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		CcpJsonRepresentation purge = JnBusinessPurgeExpiredDisposableRecords.INSTANCE.execute(json);
		CcpJsonRepresentation purgeCounters = purge.getJsonPiece(JnBusinessPurgeExpiredDisposableRecords.Fields.values());
		CcpJsonRepresentation backup = JnBusinessBackupTheDatabase.INSTANCE.execute(json);
		CcpJsonRepresentation backupWithPurge = backup.put(Fields.purge, purgeCounters);
		return backupWithPurge;
	}
}
