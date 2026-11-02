package com.jn.business.email.suppression;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.mensageria.JnBusinessPublishWriteEvent;

/**
 * Announces that an address became suppressed (the first write of {@link JnEntityEmailSuppressed}, not the rewrites of the
 * next loads) to whoever listens to it in {@code jn_write_event_subscribers}; vis inactivates the recruiter.
 */
public class JnBusinessPublishEmailSuppressed extends JnBusinessPublishWriteEvent {

	/** Names the event: the insert of {@code jn_email_suppressed}. */
	public JnBusinessPublishEmailSuppressed() {
		super(JnEntityEmailSuppressed.ENTITY, CcpEntityDecoratorOperationType.insert);
	}
}
