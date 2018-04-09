package de.tigges.tchreservation.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReservationController {

	@GetMapping("/reservation/{systemConfigId}")
	public String showReservations (@PathVariable long systemConfigId, Model model) {
		return "greeting";
	}
}
