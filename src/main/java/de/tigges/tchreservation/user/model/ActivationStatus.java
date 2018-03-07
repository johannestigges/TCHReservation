package de.tigges.tchreservation.user.model;

/**
 * status of Activation
 * <p>
 * before using a device the user and the admin must verify it.
 */
public enum ActivationStatus {
	CREATED, // created by the user, but not verified
	VERIFIER_BY_USER, // verified by user, but not verified by admin
	ACTIVE, 
	LOCKED, // locked due to too many login failures
	REMOVED, // removed by user or by admin
}
