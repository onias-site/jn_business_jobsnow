package com.jn.json.fields.validation;

/**
 * The groups of daily quota of the tickets the users open with the support ({@code JnEntityTicketDailyLimit}): each
 * group allows a single ticket per user every 24 hours, and the groups are independent of each other, so in practice a
 * user opens, once a day, one ticket of {@link #sharedTickets}, one of {@link #clarifyDoubts} and one of
 * {@link #makeCriticism}. Each ticket of the
 * platform tells its group: the skill suggestion and the skill hierarchy fix (vis) and the "contact us" (js) that is
 * not a bug report, a question nor a criticism share {@link #sharedTickets}; a question of the "contact us" has
 * {@link #clarifyDoubts} and a criticism has {@link #makeCriticism}; a bug report is {@link #unlimited}, until the support ignores the user.
 */
public enum JnTicketQuotaGroups {
	/** One ticket a day, shared by the skill suggestion, the skill hierarchy fix and most kinds of "contact us". */
	sharedTickets {
		public boolean isLimited() {
			return true;
		}
	},
	/** One question of the "contact us" a day, independent of {@link #sharedTickets}. */
	clarifyDoubts {
		public boolean isLimited() {
			return true;
		}
	},
	/** One criticism of the "contact us" a day, independent of the other groups. */
	makeCriticism {
		public boolean isLimited() {
			return true;
		}
	},
	/** No daily quota: the bug reports of the "contact us". Never recorded. */
	unlimited {
		public boolean isLimited() {
			return false;
		}
	},
	;

	/**
	 * Whether a ticket of this group spends a daily quota.
	 * @return {@code false} only for {@link #unlimited}
	 */
	public abstract boolean isLimited();
}
