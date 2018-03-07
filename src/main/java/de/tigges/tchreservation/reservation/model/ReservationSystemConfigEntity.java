package de.tigges.tchreservation.reservation.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "reservation_system")
public class ReservationSystemConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    String name;
    int courts;
    int durationUnitInMinutes;
    int openingHour;
    int closingHour;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
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
}