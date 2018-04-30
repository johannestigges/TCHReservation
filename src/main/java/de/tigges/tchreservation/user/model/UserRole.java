package de.tigges.tchreservation.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserRole {
    /**
     * user not logged in, no user data, no rights
     */
	@JsonProperty("0")
    ANONYMOUS,
    /**
     * registered user, basic rights
     */
	@JsonProperty("1")
    REGISTERED,
    /**
     * kiosk system, no user data, but special rights
     */
	@JsonProperty("2")
    KIOSK,
    /**
     * Trainer
     */
	@JsonProperty("3")
    TRAINER,
    /**
     * administrator, all rights, usr with care!
     */
	@JsonProperty("4")
    ADMIN
}
