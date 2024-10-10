package de.tigges.tchreservation.systemconfig.jpa;

import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.model.UserRole;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ReservationTypeMapper {

    public static List<SystemConfigReservationType> mapEntities(Iterable<ReservationTypeEntity> entities) {
        return entities == null ? null
                : StreamSupport.stream(entities.spliterator(), false)
                .map(ReservationTypeMapper::mapEntity)
                .toList();
    }

    public static SystemConfigReservationType mapEntity(ReservationTypeEntity entity) {
        return new SystemConfigReservationType(
                entity.getId(),
                entity.getType(),
                entity.getName(),
                mapInteger(entity.getMaxDuration(), 0),
                mapInteger(entity.getMaxDaysReservationInFuture(), 0),
                mapInteger(entity.getMaxCancelInHours(), 0),
                entity.isRepeatable(),
                entity.isPublicVisible(),
                mapForbiddenDaysOfWeek(entity.getForbiddenDaysOfWeek()),
                entity.getCssStyle(),
                mapRoles(entity.getRoles())
        );
    }

    public static Set<ReservationTypeEntity> mapTypes(Collection<SystemConfigReservationType> types) {
        return types == null
                ? null
                : types.stream().map(ReservationTypeMapper::mapType).collect(Collectors.toSet());
    }

    public static ReservationTypeEntity mapType(SystemConfigReservationType type) {
        var entity = new ReservationTypeEntity();
        entity.setId(type.id());
        entity.setType(type.type());
        entity.setName(type.name());
        entity.setMaxDuration(type.maxDuration());
        entity.setMaxDaysReservationInFuture(type.maxDaysReservationInFuture());
        entity.setMaxCancelInHours(type.maxCancelInHours());
        entity.setRepeatable(type.repeatable());
        entity.setPublicVisible(type.publicVisible());
        entity.setForbiddenDaysOfWeek(mapForbiddenDaysOfWeek(type.forbiddenDaysOfWeek()));
        entity.setCssStyle(type.cssStyle());
        entity.setRoles(mapRoles(type.roles()));
        return entity;
    }

    private static Collection<DayOfWeek> mapForbiddenDaysOfWeek(String forbiddenDaysOfWeek) {
        return StringUtils.hasText(forbiddenDaysOfWeek)
                ? Arrays.stream(forbiddenDaysOfWeek.split(",")).map(DayOfWeek::valueOf).toList()
                : Collections.emptySet();
    }

    private static String mapForbiddenDaysOfWeek(Collection<DayOfWeek> forbiddenDaysOfWeek) {
        return forbiddenDaysOfWeek == null
                ? null
                : forbiddenDaysOfWeek.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public static String mapRoles(Collection<UserRole> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    public static Collection<UserRole> mapRoles(String roles) {
        return Arrays.stream(roles.split(",")).map(UserRole::valueOf).toList();
    }

    public static Integer mapInteger(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
