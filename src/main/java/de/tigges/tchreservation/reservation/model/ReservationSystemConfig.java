package de.tigges.tchreservation.reservation.model;

import java.util.List;

public record ReservationSystemConfig(
        long id,
        String name,
        String title,
        List<String> courts,
        int durationUnitInMinutes,
        int maxDaysReservationInFuture,
        int maxDuration,
        int openingHour,
        int closingHour,
        List<SystemConfigReservationType> types) {

    public ReservationSystemConfig withTypes(List<SystemConfigReservationType> types) {
        return new ReservationSystemConfig(
                id,
                name,
                title,
                courts,
                durationUnitInMinutes,
                maxDaysReservationInFuture,
                maxDuration,
                openingHour,
                closingHour,
                types
        );
    }
}
