package de.tigges.tchreservation.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    ADMIN
}
