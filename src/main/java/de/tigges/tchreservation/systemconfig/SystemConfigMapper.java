package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeMapper;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;

import java.util.Arrays;
import java.util.List;

public class SystemConfigMapper {
    public static ReservationSystemConfig map(SystemConfigEntity entity) {
        return new ReservationSystemConfig(
                entity.getId() != null ? entity.getId() : 0,
                entity.getName(),
                entity.getTitle(),
                splitCourts(entity.getCourts()),
                entity.getDurationUnitInMinutes(),
                entity.getMaxDaysReservationInFuture(),
                entity.getMaxDuration(),
                entity.getOpeningHour(),
                entity.getClosingHour(),
                ReservationTypeMapper.mapEntities(entity.getTypes())
        );
    }

    public static SystemConfigEntity map(ReservationSystemConfig c, boolean isNew) {
        var entity = new SystemConfigEntity();
        entity.setId(c.id());
        entity.setName(c.name());
        entity.setTitle(c.title());
        entity.setCourts(joinCourts(c.courts()));
        entity.setDurationUnitInMinutes(c.durationUnitInMinutes());
        entity.setMaxDaysReservationInFuture(c.maxDaysReservationInFuture());
        entity.setMaxDuration(c.maxDuration());
        entity.setOpeningHour(c.openingHour());
        entity.setClosingHour(c.closingHour());
        entity.setNew(isNew);
        entity.setTypes(ReservationTypeMapper.mapTypes(c.types()));
        return entity;
    }

    private static List<String> splitCourts(String courts) {
        return Arrays.asList(courts.split("[,\t][\t ]*"));
    }

    private static String joinCourts(List<String> courts) {
        return String.join("\t", courts);
    }
}
