package com.jn.business.email.suppression;

import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.entities.JnEntityEmailTemporarilyBlocked;

/** Where each suppression list of the provider is kept: the lists it keeps suppressing, or the temporary refusals. */
enum JnEmailSuppressionTarget {
	/** Permanent refusals. */
	bounces(CcpEmailSuppressionList.bounces) {
		CcpEntity getEntity() {
			return JnEntityEmailSuppressed.ENTITY;
		}
	},
	/** Malformed or inexistent addresses. */
	invalid_emails(CcpEmailSuppressionList.invalid_emails) {
		CcpEntity getEntity() {
			return JnEntityEmailSuppressed.ENTITY;
		}
	},
	/** Messages marked as spam. */
	spam_reports(CcpEmailSuppressionList.spam_reports) {
		CcpEntity getEntity() {
			return JnEntityEmailSuppressed.ENTITY;
		}
	},
	/** Temporary refusals, which the provider does not suppress. */
	blocks(CcpEmailSuppressionList.blocks) {
		CcpEntity getEntity() {
			return JnEntityEmailTemporarilyBlocked.ENTITY;
		}
	}
	;

	/** The list of the provider. */
	final CcpEmailSuppressionList suppressionList;

	/**
	 * Associates the item with its list.
	 * @param suppressionList the list of the provider
	 */
	private JnEmailSuppressionTarget(CcpEmailSuppressionList suppressionList) {
		this.suppressionList = suppressionList;
	}

	/**
	 * Tells where the records of the list are kept.
	 * @return the entity
	 */
	abstract CcpEntity getEntity();
}
