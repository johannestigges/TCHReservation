package de.tigges.tchreservation.reservation.model;

import de.tigges.tchreservation.user.model.UserRole;

import java.time.DayOfWeek;
import java.util.Collection;

public record SystemConfigReservationType(
        Long id,
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
    public SystemConfigReservationType(int type, String name, int maxDuration, int maxDaysReservationInFuture, int maxCancelInHours,
                                boolean repeatable, boolean publicVisible, Collection<DayOfWeek> forbiddenDaysOfWeek, String cssStyle, Collection<UserRole> userRoles) {
        this(null, type, name, maxDuration, maxDaysReservationInFuture, maxCancelInHours,
                repeatable, publicVisible, forbiddenDaysOfWeek, cssStyle, userRoles);
    }
}
