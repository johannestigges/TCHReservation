package de.tigges.tchreservation.user.model;

public enum UserRole {
	/**
	 * user not logged in, no user data, no rights
	 */
	ANONYMOUS,
	/**
	 * registered user, basic rights
	 */
	REGISTERED,
	/**
	 * kiosk system, no user data, but special rights
	 */
	KIOSK,
	/**
	 * Trainer
	 */
	TRAINER,
	/**
	 * administrator, all rights, usr with care!
	 */
	ADMIN,
	/**
	 * technical user, can use the system, but cannot change user settings or
	 * password
	 */
	TECHNICAL,
	/**
	 * teamster, can add, modify and delete tournament reservations
	 */
	TEAMSTER,
	/**
	 * guest
	 */
	GUEST
}
