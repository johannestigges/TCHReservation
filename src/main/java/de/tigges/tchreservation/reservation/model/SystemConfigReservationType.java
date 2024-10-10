package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.user.model.UserRole;

import java.time.DayOfWeek;
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
        Collection<DayOfWeek> forbiddenDaysOfWeek,
        String cssStyle,
        Collection<UserRole> roles) {
}
