package de.tigges.tchreservation.reservation.model;

public class ReservationSystemConfig extends Named {
    private int courts;
    private int durationUnitInMinutes;
    private int openingHour;
    private int closingHour;

    public static ReservationSystemConfig of(ReservationSystemConfigEntity e) {
        ReservationSystemConfig c = new ReservationSystemConfig();
        c.id = e.getId();
        c.name = e.getName();
        c.courts = e.getCourts();
        e.durationUnitInMinutes = e.getDurationUnitInMinutes();
        e.openingHour = e.getOpeningHour();
        e.closingHour = e.getClosingHour();
        return c;
    }

    public int getCourts() {
        return courts;
    }

    public int getDurationUnitInMinutes() {
        return durationUnitInMinutes;
    }

    public int getOpeningHour() {
        return openingHour;
    }

    public int getClosingHour() {
        return closingHour;
    }

    public ReservationSystemConfigEntity toEntity() {
        ReservationSystemConfigEntity e = new ReservationSystemConfigEntity();
        e.id = getId();
        e.name = getName();
        e.courts = getCourts();
        e.durationUnitInMinutes = getDurationUnitInMinutes();
        e.openingHour = getOpeningHour();
        e.closingHour = getClosingHour();
        return e;
    }
}
