package de.tigges.tchreservation.reservation;

import java.time.LocalTime;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

public class ReservationSystemConfigUtil {

	private ReservationSystemConfigUtil() {
	}

	public static int getRows(ReservationSystemConfig config) {
		return (config.getClosingHour() - config.getOpeningHour()) * 60 / config.getDurationUnitInMinutes();
	}

	public static int toMinutes(ReservationSystemConfig config, int row) {
		return config.getOpeningHour() * 60 + row * config.getDurationUnitInMinutes();
	}

	public static String showTime(ReservationSystemConfig config, int row) {
		int hour = config.getOpeningHour() + row * config.getDurationUnitInMinutes() / 60;
		int minute = (row * config.getDurationUnitInMinutes()) % 60;
		return String.format("%02d:%02d", hour, minute);
	}

	public static int toRow(ReservationSystemConfig config, LocalTime time) {
		int minutes = time.getHour() * 60 + time.getMinute() - config.getOpeningHour() * 60;
		return minutes / config.getDurationUnitInMinutes();
	}
}
