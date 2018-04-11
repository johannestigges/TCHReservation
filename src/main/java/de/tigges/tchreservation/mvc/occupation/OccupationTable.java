package de.tigges.tchreservation.mvc.occupation;

import java.time.LocalDate;

import de.tigges.tchreservation.mvc.table.TableData;
import de.tigges.tchreservation.reservation.ReservationSystemConfigUtil;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

public class OccupationTable extends TableData {

	ReservationSystemConfig config;

	public OccupationTable(ReservationSystemConfig config) {
		this.config = config;
	}

	public void show(Iterable<Occupation> occupations, LocalDate date) {
		createEmptyTable();
		occupations.forEach(o -> addOccupation(o));
	}

	private void addOccupation(Occupation occupation) {
		int row =ReservationSystemConfigUtil.toRow(config, occupation.getStart());
		int column = occupation.getCourt();
		int rowspan = occupation.getDuration();
		int colspan = occupation.getLastCourt() - occupation.getCourt();
		
		setCell(row, column, rowspan, colspan);
		setData(row, column, occupation.getText(), occupation.getType().name());
	}

	private void createEmptyTable() {
		int rowspan = 60 / config.getDurationUnitInMinutes();

		System.out.println(rowspan);
		System.out.println(ReservationSystemConfigUtil.getRows(config));
		System.out.println(config.getName());
		System.out.println(config.getOpeningHour());
		System.out.println(config.getClosingHour());
		System.out.println(config.getCourts());
		System.out.println(config.getDurationUnitInMinutes());
		clearTable();
		for (int row = 0; row < ReservationSystemConfigUtil.getRows(config); row++) {
			boolean mainRow = row % rowspan == 0;

			// first column: time
			if (mainRow) {
				setCell(row, 0, rowspan, 1);
				setData(row, 0, ReservationSystemConfigUtil.showTime(config, row), "time");
			}
			// court columns
			for (int c = 0; c < config.getCourts(); c++) {
				if (mainRow) {
					setCell(row, c + 1, rowspan, 1);
				}
			}
		}
	}
}
