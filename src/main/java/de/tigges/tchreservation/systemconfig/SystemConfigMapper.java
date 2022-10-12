package de.tigges.tchreservation.systemconfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;

public class SystemConfigMapper {
	public static ReservationSystemConfig map(SystemConfigEntity entity) {
		// @formatter:off
 		return new ReservationSystemConfig(
 				entity.getId(), 
 				entity.getName(), 
				entity.getTitle(),
 				splitCourts(entity.getCourts()),
				entity.getDurationUnitInMinutes(), 
				entity.getMaxDaysReservationInFuture(), 
				entity.getMaxDuration(),
				entity.getOpeningHour(), 
				entity.getClosingHour()
				);
		// @formatter:on
	}

	public static SystemConfigEntity map(ReservationSystemConfig c) {
		SystemConfigEntity entity = new SystemConfigEntity();
		entity.setId(c.getId());
		entity.setName(c.getName());
		entity.setTitle(c.getTitle());
		entity.setCourts(c.getCourts().stream().collect(Collectors.joining("\t")));
		entity.setDurationUnitInMinutes(c.getDurationUnitInMinutes());
		entity.setMaxDaysReservationInFuture(c.getMaxDaysReservationInFuture());
		entity.setMaxDuration(c.getMaxDuration());
		entity.setOpeningHour(c.getOpeningHour());
		entity.setClosingHour(c.getClosingHour());
		return entity;
	}

	private static List<String> splitCourts(String courts) {
		return Arrays.asList(courts.split("[,\t][\t ]*"));
	}
}
