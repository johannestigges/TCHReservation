package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.user.model.UserRole;

import java.util.Collection;

public record SystemConfigReservationType(
        long id,
        int type,
        String name,
        int maxDuration,
        int maxDaysReservationInFuture,
        int maxCancelInHours,

        boolean repeatable,
        boolean publicVisible,
        Collection<UserRole> roles) {
}

