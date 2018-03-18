package de.tigges.tchreservation.reservation;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

@RestController
@RequestMapping("/reservation")
public class ReservationService {

	public static final String STATUS_OK = "Ok";

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;

	/**
	 * constructor with injection
	 * 
	 * @param reservationRepository
	 * @param occupationRepository
	 */
	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository systemConfigRepository) {
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = systemConfigRepository;
	}

	/**
	 * add a reservation
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/add", method = RequestMethod.POST)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {
		checkReservation(reservation);
		return reservationRepository.save(reservation);
	}

	/**
	 * check a reservation
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/check", method = RequestMethod.POST)
	public void checkReservation(Reservation reservation) {

		if (reservation == null) {
			throw new ReservationException("no reservation data");
		}

		if (reservation.getSystemConfig() == null || reservation.getSystemConfig().getId() <= 0) {
			throw new ReservationException("no reservation system");
		}

		if (reservation.getSystemConfig().getOpeningHour() <= 0) {
			throw new ReservationException("no reservation system config opening hour");
		}

		if (reservation.getSystemConfig().getClosingHour() <= 0) {
			throw new ReservationException("no reservation system config closing hour");
		}

		if (isEmpty(reservation.getName())) {
			throw new ReservationException("no reservation name");
		}

		LocalDateTime start = reservation.getStart();
		if (start == null) {
			throw new ReservationException("no start time");
		}
		if (start.isBefore(LocalDateTime.now())) {
			throw new ReservationException("start time in the past ist not allowed");
		}

		if (start.getHour() < reservation.getSystemConfig().getOpeningHour()) {
			throw new ReservationException("start hour %02d:00 before opening hour %02d:00 not allowed", //
					start.getHour(), reservation.getSystemConfig().getOpeningHour());
		}
		if (start.getHour() > reservation.getSystemConfig().getClosingHour()) {
			throw new ReservationException("start hour %02d:00 after closing hour %02d:00 not allowed", //
					start.getHour(), reservation.getSystemConfig().getClosingHour());
		}
		if (start.getMinute() != 0
				&& start.getMinute() % reservation.getSystemConfig().getDurationUnitInMinutes() != 0) {
			throw new ReservationException("start time with %d minutes not allowed", start.getMinute());
		}
		if (reservation.getDuration() < 1) {
			throw new ReservationException("duration must be > 0");
		}
		if (reservation.getCourts() == null) {
			throw new ReservationException("no courts");
		}
		if (reservation.getCourts().length < 1) {
			throw new ReservationException("no courts");
		}
		if (reservation.getCourts().length > reservation.getSystemConfig().getCourts()) {
			throw new ReservationException("more than %d courts not allowed",
					reservation.getSystemConfig().getCourts());
		}

		for (int i = 0; i < reservation.getCourts().length; i++) {
			int court = reservation.getCourts()[i];
			if (court < 1) {
				throw new ReservationException("court[%d]: %d < 1 not allowed", i, reservation.getCourts()[i]);
			}
			if (court > reservation.getSystemConfig().getCourts()) {
				throw new ReservationException("court[%d]: %d > %d not allowed", //
						i, reservation.getCourts()[i], reservation.getSystemConfig().getCourts());
			}
		}
	}

	/**
	 * get all occupations
	 * 
	 * @return list of all occupations
	 */
	@RequestMapping(path = "/getOccupations", method = RequestMethod.GET)
	public Iterable<Occupation> getOccupations( //
	// @RequestParam(value = "from") @DateTimeFormat(iso = ISO.DATE) Date from,
	// @RequestParam(value = "until") @DateTimeFormat(iso = ISO.DATE) Date until
	) {
		return occupationRepository.findAll();
	}

	/**
	 * get all reservations for one user
	 * 
	 * @param userId
	 * @return all reservations belonging to that user private boolean
	 *         isEmpty(String s) { return s == null || s.trim().isEmpty(); }
	 * 
	 * 
	 */
	@RequestMapping(path = "/get/{user}", method = RequestMethod.GET)
	public Iterable<Reservation> getReservations(@RequestParam("user") long userId) {
		return reservationRepository.findByUserId(userId);
	}

	/**
	 * get the reservation system configuration
	 * 
	 * @param systemId
	 * @return
	 */
	@RequestMapping(path = "/getSystemConfig/{id}", method = RequestMethod.GET)
	public @ResponseBody ReservationSystemConfig getSystemConfig(@RequestParam("id") long id) {
		return systemConfigRepository.findById(id)
				.orElseThrow(() -> new ReservationException("no reservation system config with id %d",id));
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}
