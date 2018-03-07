package de.tigges.tchreservation.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationEntity;
import de.tigges.tchreservation.reservation.model.ReservationRepository;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfigEntity;
import de.tigges.tchreservation.reservation.model.ReservationSystemRespoitory;

@RestController
public class ReservationService {

	public static final String STATUS_OK = "Ok";

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemRespoitory reservationSystemRespoitory;

	/**
	 * constructor with injection
	 * 
	 * @param reservationRepository
	 * @param occupationRepository
	 */
	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemRespoitory reservationSystemRespoitory) {
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.reservationSystemRespoitory = reservationSystemRespoitory;
	}

	/**
	 * add a reservation
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/addReservation", method = RequestMethod.POST)
	public @ResponseBody String addReservation(@RequestBody Reservation reservation) {
		System.out.println("add " + reservation);
		String status = checkReservation(reservation);
		if (!STATUS_OK.equals(status)) {
			System.out.println("return error:" + status);
			return status;
		}
		ReservationEntity reservationEntity = reservationRepository.save(reservation.toEntity());
		return String.valueOf(Reservation.of(reservationEntity).getId());
	}

	/**
	 * check a reservation
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/checkReservation", method = RequestMethod.POST)
	public @ResponseBody String checkReservation(Reservation reservation) {

		if (reservation == null) {
			return "no reservation data";
		}

		if (reservation.getSystemId() <= 0) {
			return "no reservation system id";
		}

		ReservationSystemConfigEntity system = reservationSystemRespoitory.findById(reservation.getSystemId())
				.orElseThrow(() -> new IllegalStateException(
						"no reservation system config found for id " + reservation.getSystemId()));

		if (system == null) {
			return String.format("reservation system %d not found", reservation.getSystemId());
		}

		if (isEmpty(reservation.getName())) {
			return "no reservation name";
		}
		LocalDateTime start = reservation.getStart();
		if (start == null) {
			return "no start time";
		}
		if (start.isBefore(LocalDateTime.now())) {
			return "start time in the past ist not allowed";
		}
		if (start.getHour() < system.getOpeningHour()) {
			return String.format("start hour %02d:00 before opening hour %02d:00 not allowed", //
					start.getHour(), system.getOpeningHour());
		}
		if (start.getHour() > system.getClosingHour()) {
			return String.format("start hour %02d:00 after closing hour %02d:00 not allowed", //
					start.getHour(), system.getClosingHour());
		}
		if (start.getMinute() != 0 && start.getMinute() % system.getDurationUnitInMinutes() != 0) {
			return String.format("start time with %d minutes not allowed", start.getMinute());
		}
		if (reservation.getDuration() < 1) {
			return "duration must be > 0";
		}
		if (reservation.getCourts() == null) {
			return "no courts";
		}
		if (reservation.getCourts().length < 1) {
			return "no courts";
		}
		if (reservation.getCourts().length > system.getCourts()) {
			return String.format("more than %d courts not allowed", system.getCourts());
		}

		for (int i = 0; i < reservation.getCourts().length; i++) {
			int court = reservation.getCourts()[i];
			if (court < 1) {
				return String.format("court[%d]: %d < 1 not allowed", i, reservation.getCourts()[i]);
			}
			if (court > system.getCourts()) {
				return String.format("court[%d]: %d > %d not allowed", //
						i, reservation.getCourts()[i], system.getCourts());
			}
		}

		return STATUS_OK;
	}

	/**
	 * get all occupations
	 * 
	 * @return list of all occupations
	 */
	@RequestMapping(path = "/getOccupations", method = RequestMethod.GET)
	public List<Occupation> getOccupations( //
	// @RequestParam(value = "from") @DateTimeFormat(iso = ISO.DATE) Date from,
	// @RequestParam(value = "until") @DateTimeFormat(iso = ISO.DATE) Date until
	) {
		return Occupation.of(occupationRepository.findAll());
	}

	/**
	 * get all reservations for one user
	 * 
	 * @param userId
	 * @return all reservations belonging to that user	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}


	 */
	@RequestMapping(path = "/getReservations/{user}", method = RequestMethod.GET)
	public List<Reservation> getReservations(@RequestParam("user") long userId) {
		return Reservation.of(reservationRepository.findByUserId(userId));
	}

	/**
	 * get the reservation system configuration
	 * 
	 * @param systemId
	 * @return
	 */
	@RequestMapping(path = "/getSystemConfig/{systemId}", method = RequestMethod.GET)
	public ReservationSystemConfig getSystemConfig(@RequestParam("systemId") long systemId) {
		ReservationSystemConfigEntity entity = reservationSystemRespoitory.findById(systemId)
				.orElseThrow(() -> new IllegalStateException("no reservation system config " + systemId));
		return ReservationSystemConfig.of(entity);
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}
