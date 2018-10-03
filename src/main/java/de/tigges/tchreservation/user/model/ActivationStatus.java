package de.tigges.tchreservation.user.model;

import de.tigges.tchreservation.user.ActivationStatusException;

/**
 * status of Activation
 * <p>
 * before using a device the user and the admin must verify it.
 */
public enum ActivationStatus {
	CREATED, // created by the user, but not verified
	VERIFIED_BY_USER, // verified by user, but not verified by admin
	ACTIVE, // verified by user and by admin
	LOCKED, // locked due to too many login failures
	REMOVED; // removed by user or by admin

	public static void checkStatusChange(ActivationStatus from, ActivationStatus to, String id) {
		if (from != null && to != null) {
			checkStatusInRange(from, to, id, VERIFIED_BY_USER, CREATED);
			checkStatusInRange(from, to, id, ACTIVE, VERIFIED_BY_USER, LOCKED, REMOVED);
			checkStatusInRange(from, to, id, LOCKED, ACTIVE);
			checkStatusInRange(from, to, id, REMOVED, ACTIVE);
		}
	}

	private static void checkStatusInRange(ActivationStatus from, ActivationStatus to, String id,
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
