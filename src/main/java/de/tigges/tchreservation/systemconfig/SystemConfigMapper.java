package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeMapper;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;

import java.util.Arrays;
import java.util.List;

public class SystemConfigMapper {
    public static ReservationSystemConfig map(SystemConfigEntity entity) {
        return new ReservationSystemConfig(
                entity.getId(),
                entity.getName(),
                entity.getTitle(),
                splitCourts(entity.getCourts()),
                entity.getDurationUnitInMinutes(),
                entity.getMaxDaysReservationInFuture(),
                entity.getMaxDuration(),
                entity.getOpeningHour(),
                entity.getClosingHour(),
                ReservationTypeMapper.map(entity.getTypes())
        );
    }

    public static SystemConfigEntity map(ReservationSystemConfig c) {
        SystemConfigEntity entity = new SystemConfigEntity();
        entity.setId(c.getId());
        entity.setName(c.getName());
        entity.setTitle(c.getTitle());
        entity.setCourts(String.join("\t", c.getCourts()));
        entity.setDurationUnitInMinutes(c.getDurationUnitInMinutes());
        entity.setMaxDaysReservationInFuture(c.getMaxDaysReservationInFuture());
        entity.setMaxDuration(c.getMaxDuration());
        entity.setOpeningHour(c.getOpeningHour());
        entity.setClosingHour(c.getClosingHour());
        entity.setTypes(ReservationTypeMapper.map(c.getTypes()));
        return entity;
    }

    private static List<String> splitCourts(String courts) {
        return Arrays.asList(courts.split("[,\t][\t ]*"));
    }
}
