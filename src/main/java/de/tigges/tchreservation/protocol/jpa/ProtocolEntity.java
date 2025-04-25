package de.tigges.tchreservation.protocol.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.exception.RestException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.user.jpa.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "protocol")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProtocolEntity {

    public ProtocolEntity(Protocollable entity, ActionType actionType, UserEntity user) {
        setTime(LocalDateTime.now());
        setEntityType(entity.protocolEntityType());
        setEntityId(entity.protocolEntityId());
        setValue(toJson(entity.protocolFields()));
        setActionType(actionType);
        setUser(user);
    }

    public ProtocolEntity(Protocollable entity, Protocollable oldEntity, UserEntity user) {
        this(entity, ActionType.MODIFY, user);
        this.setOldValue(toJson(oldEntity.protocolFields()));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private long entityId;

    @Column(nullable = false)
    private ActionType actionType;

    @Column(length = 5000, nullable = false, name = "\"value\"")
    private String value;

    @Column(length = 5000)
    private String oldValue;

    @ManyToOne(optional = false)
    private UserEntity user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProtocolEntity that = (ProtocolEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private String toJson(Object o) {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.JSON_SERIALIZATION,
                    "error writing protocol element %s:%s".formatted(o.toString(), e.getMessage()));
        }
    }
}
