package com.jn.business.ignored;

import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.jn.mensageria.JnBusinessPublishWriteEvent;

/**
 * Announces that the support operator ignored a user ({@link JnEntityCommandNotAllowedToUser} saved): each cost center
 * that keeps requests of the users (vis: skill suggestions and skill hierarchy fixes; js: tickets of the "contact us")
 * listens to it in {@code jn_write_event_subscribers} and discards the requests of the user still pending, as if they
 * had never existed. Until 2026-10-23 the entity belonged to vis, which discarded its own requests directly.
 */
public class JnBusinessPublishIgnoredUserSaved extends JnBusinessPublishWriteEvent {

	/** Names the event: the save of {@code jn_command_not_allowed_to_user}. */
	public JnBusinessPublishIgnoredUserSaved() {
		super(JnEntityCommandNotAllowedToUser.ENTITY, CcpEntityDecoratorOperationType.save);
	}
}
