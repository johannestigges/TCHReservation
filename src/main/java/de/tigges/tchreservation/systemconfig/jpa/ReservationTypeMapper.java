package de.tigges.tchreservation.systemconfig.jpa;

import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.model.UserRole;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ReservationTypeMapper {

    public static List<SystemConfigReservationType> map(Iterable<ReservationTypeEntity> entities) {
        return entities == null ? null
                : StreamSupport.stream(entities.spliterator(), false)
                .map(ReservationTypeMapper::map)
                .toList();
    }

    public static SystemConfigReservationType map(ReservationTypeEntity entity) {
        return new SystemConfigReservationType(
                entity.getId(),
                entity.getType(),
                entity.getName(),
                mapInteger(entity.getMaxDuration(), 0),
                mapInteger(entity.getMaxDaysReservationInFuture(), 0),
                mapInteger(entity.getMaxCancelInHours(), 0),
                entity.isRepeatable(),
                entity.isPublicVisible(),
                mapRoles(entity.getRoles())
        );
    }


    public static Collection<UserRole> mapRoles(String roles) {
        return Arrays.stream(roles.split(",")).map(UserRole::valueOf).toList();
    }

    public static Set<ReservationTypeEntity> map(Collection<SystemConfigReservationType> types) {
        return types == null ? null
                : types.stream().map(ReservationTypeMapper::map).collect(Collectors.toSet());
    }

    public static ReservationTypeEntity map(SystemConfigReservationType type) {
        var entity = new ReservationTypeEntity();
        entity.setId(type.id());
        entity.setType(type.type());
        entity.setName(type.name());
        entity.setMaxDuration(type.maxDuration());
        entity.setMaxDaysReservationInFuture(type.maxDaysReservationInFuture());
        entity.setMaxCancelInHours(type.maxCancelInHours());
        entity.setRepeatable(type.repeatable());
        entity.setPublicVisible(type.publicVisible());
        entity.setRoles(mapRoles(type.roles()));
        return entity;
    }

    public static String mapRoles(Collection<UserRole> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public static Integer mapInteger(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
