package de.tigges.tchreservation.reservation.model;

import javax.persistence.*;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "reservation")
public class ReservationEntity {

    @Id
    @GeneratedValue
    long id;
    String name;
    long userId;
    LocalDateTime start;
    LocalDate weeklyRepeatUntil;
    String courts;
    int duration;
    String type;

    @ManyToOne
    @JoinColumn(name = "settings_id")
    ReservationSystemConfigEntity system;

    @OneToMany(mappedBy = "reservation", targetEntity = OccupationEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<OccupationEntity> occupations;

    long getSystemId() {
        return system == null ? 0 : system.id;
    }
}
