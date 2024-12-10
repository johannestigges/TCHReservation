package de.tigges.tchreservation.systemconfig.jpa;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "systemconfig")
@Data
@NoArgsConstructor
public class SystemConfigEntity implements Protocollable {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column()
    private String title;

    @Column(nullable = false)
    private String courts;

    @Column(nullable = false)
    private int durationUnitInMinutes;

    @Column(nullable = false)
    private int maxDaysReservationInFuture;

    @Column(nullable = false)
    private int maxDuration;

    @Column(nullable = false)
    private int openingHour;

    @Column(nullable = false)
    private int closingHour;

    @Transient
    private Set<ReservationTypeEntity> types = new HashSet<>();

    @Override
    public Map<String, String> protocolFields() {
        return protocolFields( //
                "id", Long.toString(id), //
                "name", name, //
                "title", title, //
                "courts", courts, //
                "durationUnitInMinutes", Integer.toString(durationUnitInMinutes), //
                "maxDaysReservationInFuture", Integer.toString(maxDaysReservationInFuture), //
                "maxDuration", Integer.toString(maxDuration), //
                "openingHour", Integer.toString(openingHour), //
                "closingHour", Integer.toString(closingHour));
    }

    @Override
    public EntityType protocolEntityType() {
        return EntityType.SYSTEM_CONFIGURATION;
    }

    @Override
    public long protocolEntityId() {
        return id;
    }
}
