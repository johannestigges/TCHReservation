package de.tigges.tchreservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

public class ReservationSystemConfigUtilTest {

	@Test
	public void testGetRows() {
		// opening hour, closing hour, durationUnitInMinutes, expectedRows
		checkGetRows(8, 9, 60, 1);
		checkGetRows(0, 24, 120, 12);
		checkGetRows(8, 23, 60, 15);
		checkGetRows(8, 20, 30, 24);
	}

	@Test
	public void testToMinutes() {
		// opening hour, durationUnitInMinutes, row, expectedMinutes
		checkToMinutes(0, 60, 0, 0);
		checkToMinutes(0, 60, 10, 600);
		checkToMinutes(8, 30, 6, 8 * 60 + 6 * 30);
		checkToMinutes(0, 1, 98, 98);
		checkToMinutes(11, 7, 10, 11 * 60 + 7 * 10);
	}

	@Test
	public void testToRow() {
		// opening hour, durationUnitInMinutes, endTime,expectedRows
		checkToRow(8, 30, "15:30", 15);
		checkToRow(0, 60, "17:00", 17);
		checkToRow(11, 20, "20:05", 27);
	}

	@Test
	public void testGetCourtsAsList() {
		ReservationSystemConfig config = new ReservationSystemConfig(1, "name", 3, 60, 8, 20);
		int[] courtsAsList = ReservationSystemConfigUtil.getCourtsAsList(config);
		List<Integer> collect = Arrays.stream(courtsAsList).boxed().collect(Collectors.toList());
		assertThat(collect).contains(0, 1, 2);
	}

	@Test
	public void testShowTime() {
		checkShowTime(8, 30, 0, "08:00");
		checkShowTime(8, 30, 1, "08:30");
		checkShowTime(8, 30, 11, "13:30");
		checkShowTime(10, 20, 12, "14:00");
	}

	private void checkShowTime(int openingHour, int durationUnitInMinutes, int row, String expectedTime) {
		assertThat(ReservationSystemConfigUtil.showTime(createConfig(openingHour, 22, durationUnitInMinutes), row))
				.isEqualTo(expectedTime);
	}

	private void checkGetRows(int openingHour, int closingHour, int durationUnitInMinutes, int expectedRows) {
		assertThat(ReservationSystemConfigUtil.getRows(createConfig(openingHour, closingHour, durationUnitInMinutes)))
				.isEqualTo(expectedRows);
	}

	private void checkToMinutes(int openingHour, int durationUnitInMinutes, int row, int expectedMinutes) {
		assertThat(ReservationSystemConfigUtil.toMinutes(createConfig(openingHour, 23, durationUnitInMinutes), row))
				.isEqualTo(expectedMinutes);
	}

	private void checkToRow(int openingHour, int durationUnitInMinutes, String time, int expectedRows) {
		assertThat(ReservationSystemConfigUtil.toRow(createConfig(openingHour, 23, durationUnitInMinutes),
				LocalTime.parse(time))).isEqualTo(expectedRows);
	}

	private ReservationSystemConfig createConfig(int openingHour, int closingHour, int durationUnitInMinutesk) {
		return new ReservationSystemConfig(1, "any", 1, durationUnitInMinutesk, openingHour, closingHour);
	}
}
