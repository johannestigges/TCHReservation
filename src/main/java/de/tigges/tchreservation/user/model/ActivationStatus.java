package de.tigges.tchreservation.user.model;

import de.tigges.tchreservation.user.ActivationStatusException;

/**
 * status of Activation
 * <p>
 * before using a device the user and the admin must verify it.
 * <p>
 * valid status changes CREATED -> VERIFIED_BY_USER user has activated
 * VERIFIED_BY_USER -> ACTIVE admin has activated ACTIVE -> LOCKED account is
 * locked ACTIVE -> REMOVED account inactive LOCKED -> ACTIVE admin has relocked
 * LOCKED -> REMOVED REMOVED-> ACTIVE admin has reactivated
 */
public enum ActivationStatus {
	CREATED, // created by the user, but not verified
	VERIFIED_BY_USER, // verified by user, but not verified by admin
	ACTIVE, // verified by user and by admin
	LOCKED, // locked due to too many login failures
	REMOVED; // removed by user or by admin

	public static void checkStatusChange(ActivationStatus from, ActivationStatus to, String id) {
		if (from != null && to != null && !from.equals(to)) {
			// never go back to CREATED
			checkStatusChangeInRange(from, to, id, CREATED);
			// only from CREATED -> VERIFIED
			checkStatusChangeInRange(from, to, id, VERIFIED_BY_USER, CREATED);
			// can always go to ACTIVE
			checkStatusChangeInRange(from, to, id, ACTIVE, VERIFIED_BY_USER, LOCKED, REMOVED);
			// only from ACTIVE -> LOCKED
			checkStatusChangeInRange(from, to, id, LOCKED, ACTIVE);
			checkStatusChangeInRange(from, to, id, REMOVED, ACTIVE, LOCKED);
		}
	}

	private static void checkStatusChangeInRange(ActivationStatus from, ActivationStatus to, String id,
			ActivationStatus check, ActivationStatus... range) {
		if (to.equals(check)) {
			for (int i = 0; i < range.length; i++) {
				if (from.equals(range[i])) {
					return;
				}
			}
			throw new ActivationStatusException(from, to, id);
		}
	}
}
