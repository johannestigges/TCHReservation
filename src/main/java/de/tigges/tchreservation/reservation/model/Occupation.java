package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Occupation extends ReservationSystemConfigId {
    private int court;
    private LocalDate start;
    private int duration;
    private OccupationType type;

    public static Occupation of(OccupationEntity entity) {
        Occupation occupation = new Occupation();
        occupation.systemId = entity.getSystemId();
        occupation.id = entity.id;
        occupation.name = entity.name;
        occupation.court = entity.court;
        occupation.start = entity.start;
        occupation.duration = entity.duration;
        occupation.type = OccupationType.valueOf(entity.type);
        return occupation;
    }

    public static List<Occupation> of(Iterable<OccupationEntity> entities) {
        List<Occupation> list = new ArrayList<>();
        entities.forEach(e -> list.add(Occupation.of(e)));
        return list;
    }

    public int getCourt() {
        return court;
    }

    public LocalDate getStart() {
        return start;
    }

    public int getDuration() {
        return duration;
    }

    public OccupationType getType() {
        return type;
    }

    public OccupationEntity toEntity() {
        OccupationEntity entity = new OccupationEntity();
        ReservationSystemConfigEntity system = new ReservationSystemConfigEntity();
        system.id = this.systemId;
        entity.system = system;
        entity.id = this.id;
        entity.name = this.name;
        entity.court = this.court;
        entity.start = this.start;
        entity.duration = this.duration;
        entity.type = this.type.name();
        return entity;
    }

}
