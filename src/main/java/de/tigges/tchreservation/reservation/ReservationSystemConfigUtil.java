package de.tigges.tchreservation.reservation;

import java.time.LocalTime;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

public class ReservationSystemConfigUtil {

	private ReservationSystemConfigUtil() {
	}

	public static int getRows(ReservationSystemConfig config) {
		return (config.closingHour() - config.openingHour()) * 60 / config.durationUnitInMinutes();
	}

	public static int toMinutes(ReservationSystemConfig config, int row) {
		return config.openingHour() * 60 + row * config.durationUnitInMinutes();
	}

	public static String showTime(ReservationSystemConfig config, int row) {
		int hour = config.openingHour() + row * config.durationUnitInMinutes() / 60;
		int minute = (row * config.durationUnitInMinutes()) % 60;
		return String.format("%02d:%02d", hour, minute);
	}

	public static int toRow(ReservationSystemConfig config, LocalTime time) {
		int minutes = time.getHour() * 60 + time.getMinute() - config.openingHour() * 60;
		return minutes / config.durationUnitInMinutes();
	}
}
