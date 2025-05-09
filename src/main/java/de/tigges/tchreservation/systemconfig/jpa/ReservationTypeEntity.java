package de.tigges.tchreservation.systemconfig.jpa;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Entity
@Table(name = "reservation_type")
@Data
@NoArgsConstructor
public class ReservationTypeEntity implements Protocollable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SystemConfigEntity systemConfig;

    @Column(nullable = false)
    private int type;

    @Column(nullable = false)
    private String name;

    private Integer maxDuration;
    private Integer maxDaysReservationInFuture;
    private Integer maxCancelInHours;
    @Column(columnDefinition = "boolean default false")
    private boolean repeatable;
    @Column(columnDefinition = "boolean default false")
    private boolean publicVisible;
    private String forbiddenDaysOfWeek;
    private String cssStyle;
    @Column(nullable = false)
    private String roles;

    @Override
    public Map<String, String> protocolFields() {
        return protocolFields(
                "systemConfig", systemConfig.getId() == null ? "" : Long.toString(systemConfig.getId()),
                "type", Integer.toString(type),
                "name", name,
                "maxDuration", Integer.toString(maxDuration),
                "maxDaysReservationInFuture", Integer.toString(maxDaysReservationInFuture),
                "maxCancelInHours", Integer.toString(maxCancelInHours),
                "repeatable", Boolean.toString(repeatable),
                "publicVisible", Boolean.toString(publicVisible),
                "forbiddenDaysOfWeek", forbiddenDaysOfWeek,
                "cssStyle", cssStyle,
                "roles", roles //
        );
    }

    @Override
    public EntityType protocolEntityType() {
        return EntityType.RESERVATION_TYPE;
    }

    @Override
    public long protocolEntityId() {
        return id;
    }
}
