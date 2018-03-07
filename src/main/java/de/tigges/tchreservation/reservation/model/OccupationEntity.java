package de.tigges.tchreservation.reservation.model;

import javax.persistence.*;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity(name = "occupation")
public class OccupationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    String name;
    int court;
    LocalDate start;
    int duration;
    String type;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    ReservationEntity reservation;

    @ManyToOne
    @JoinColumn(name = "system_id")
    ReservationSystemConfigEntity system;

    long getSystemId() {
        return system == null ? 0 : system.id;
    }
}
