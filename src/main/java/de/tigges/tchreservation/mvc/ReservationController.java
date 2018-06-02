package de.tigges.tchreservation.mvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import de.tigges.tchreservation.mvc.occupation.OccupationTable;
import de.tigges.tchreservation.reservation.ReservationService;
import de.tigges.tchreservation.reservation.ReservationSystemConfigRepository;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

@Controller
public class ReservationController {

	@Autowired
	ReservationSystemConfigRepository systemRepository;

	@Autowired
	ReservationService reservationService;

	@GetMapping("/mvc/reservation/{systemConfigId}/{date}")
	public String showReservations(@PathVariable long systemConfigId,
			@PathVariable(required = false, value = "0") Long date, Model model) {

		ReservationSystemConfig config = systemRepository.get(systemConfigId);
		OccupationTable table = new OccupationTable(config);
		LocalDate localDate = date == null || date.equals(0) ? LocalDate.now()
				: Instant.ofEpochMilli(date).atZone(TimeZone.getDefault().toZoneId()).toLocalDate();
	
		table.show(reservationService.getOccupations(systemConfigId, date), localDate);
		model.addAttribute("table", table.getTable());
		model.addAttribute("courts", courts(config.getCourts()));

		return "reservation";
	}
	
	private String[] courts(int courts) {
		String[] result = new String[courts];
		for (int i=0;i<courts;i++) {
			result[i] = "" + (i+1);
		}
		return result;
		
	}
}
