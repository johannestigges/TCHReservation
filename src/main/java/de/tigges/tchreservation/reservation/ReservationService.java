package de.tigges.tchreservation.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RestController
@RequestMapping("/reservation")
public class ReservationService {

	public static final String STATUS_OK = "Ok";

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;
	private UserRepository userRespository;

	/**
	 * constructor with injection
	 */
	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository systemConfigRepository, UserRepository userRepository) {
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = systemConfigRepository;
		this.userRespository = userRepository;
	}

	/**
	 * add a reservation
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/add", method = RequestMethod.POST)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {

		// check reservation data consistency
		checkReservation(reservation);

		// check Authorization

		// createOccupations
		List<Occupation> occupations = createOccupations(reservation);
		occupations.forEach(o -> checkOccupation(o));

		Reservation savedReservation = reservationRepository.save(reservation);
		occupationRepository.saveAll(occupations);
		return savedReservation;
	}

	/**
	 * check a reservation
	 * <p>
	 * <li>data consistency checks
	 * <li>authorization checks
	 * 
	 * @param reservation
	 * @return error message or OK
	 */
	@RequestMapping(path = "/check", method = RequestMethod.POST)
	public void checkReservation(Reservation reservation) {

		// data consistency checks
		if (reservation == null) {
			throw new ReservationException("no reservation data");
		}

		if (isEmpty(reservation.getText())) {
			throw new ReservationException("no reservation text");
		}

		if (reservation.getSystemConfig() == null || reservation.getSystemConfig().getId() <= 0) {
			throw new ReservationException("no reservation system");
		}

		ReservationSystemConfig systemConfig = systemConfigRepository.findById(reservation.getSystemConfig().getId())
				.orElseThrow(() -> new ReservationException("no reservation system with id %d",
						reservation.getSystemConfig().getId()));
		reservation.setSystemConfig(systemConfig);

		if (reservation.getUser() == null || reservation.getUser().getId() <= 0) {
			throw new ReservationException("no user");
		}
		User user = userRespository.findById(reservation.getUser().getId())
				.orElseThrow(() -> new ReservationException("no user with id %d", reservation.getUser().getId()));
		reservation.setUser(user);

		LocalDate date = reservation.getDate();
		if (date == null) {
			throw new ReservationException("no date");
		}

		if (date.isBefore(LocalDate.now())) {
			throw new ReservationException("date in the past is not allowed");
		}

		LocalTime start = reservation.getStart();
		if (start == null) {
			throw new ReservationException("no start time");
		}

		if (date.isEqual(LocalDate.now()) && start.isBefore(LocalTime.now())) {
			throw new ReservationException("start time in the past is not allowed");
		}

		if (start.getHour() < systemConfig.getOpeningHour()) {
			throw new ReservationException("start hour %02d:00 before opening hour %02d:00 not allowed", //
					start.getHour(), systemConfig.getOpeningHour());
		}

		if (start.getHour() > systemConfig.getClosingHour()) {
			throw new ReservationException("start hour %02d:00 after closing hour %02d:00 not allowed", //
					start.getHour(), systemConfig.getClosingHour());
		}

		if (start.getMinute() != 0 && start.getMinute() % systemConfig.getDurationUnitInMinutes() != 0) {
			throw new ReservationException("start time with %d minutes not allowed", start.getMinute());
		}

		if (LocalTime.of(start.getHour(), start.getMinute())
				.plusMinutes(reservation.getDuration() * systemConfig.getDurationUnitInMinutes())
				.isAfter(LocalTime.of(systemConfig.getClosingHour(), 0))) {
			throw new ReservationException("starttime plus duration greater than closing hour.");
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

		if (reservation.getCourts().length > systemConfig.getCourts()) {
			throw new ReservationException("more than %d courts not allowed", systemConfig.getCourts());
		}

		for (int i = 0; i < reservation.getCourts().length; i++) {
			int court = reservation.getCourts()[i];
			if (court < 1) {
				throw new ReservationException("court[%d]: %d < 1 not allowed", i, reservation.getCourts()[i]);
			}
			if (court > systemConfig.getCourts()) {
				throw new ReservationException("court[%d]: %d > %d not allowed", i, reservation.getCourts()[i],
						systemConfig.getCourts());
			}
		}
		
		// authorization checks
		if (user.hasRole(UserRole.ANONYMOUS)) {
			throw new AuthorizationException("user with role ANONYMOUS cannot add reservation.");
		}

		if (user.hasRole(UserRole.REGISTERED)) {
			if (!ReservationType.INDIVIDUAL.equals(reservation.getType())) {
				throw new AuthorizationException("user %s with role REGISTERED cannot add reservation of type %s.",
						user.getName(), reservation.getType());
			}
			if (reservation.getDuration() > 3) {
				throw new AuthorizationException(
						"user %s with role REGISTERED cannot add reservation with duration %d.", user.getName(),
						reservation.getDuration());
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
	 * @return all reservations belonging to that user  
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
				.orElseThrow(() -> new ReservationException("no reservation system config with id %d", id));
	}

	/**
	 * create all occupations for a reservation
	 * 
	 * TODO: handle multiple courts
	 * TOTO: handle repeat weekly until
	 * 
	 * @param reservation
	 * @return list of created occupations
	 */
	private List<Occupation> createOccupations(Reservation reservation) {
		List<Occupation> occupations = new ArrayList<>();
		Occupation occupation = new Occupation();
		occupation.setSystemConfig(reservation.getSystemConfig());
		occupation.setReservation(reservation);
		occupation.setText(reservation.getText());
		occupation.setType(reservation.getType());
		occupation.setCourt(reservation.getCourts()[0]);
		occupation.setDate(reservation.getDate());
		occupation.setStart(reservation.getStart());
		occupation.setDuration(reservation.getDuration());
		occupations.add(occupation);
		return occupations;
	}

	private void checkOccupation(Occupation occupation) {
		occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfig().getId(), occupation.getDate())
				.forEach(o -> checkOverlap(occupation, o));
	}

	private void checkOverlap(Occupation o1, Occupation o2) {
		if (o1.getSystemConfig().getId() != o2.getSystemConfig().getId()) {
			return;
		}
		if (o1.getCourt() != o2.getCourt()) {
			return;
		}
		if (!o1.getDate().isEqual(o2.getDate())) {
			return;
		}
		// check time overlap
		if (o1.getStart().isBefore(getEnd(o2)) && getEnd(o1).isAfter(o2.getStart())) {
			throw new ReservationNotAvailableException("overlap!");
		}
	}

	private LocalTime getEnd(Occupation o) {
		return LocalTime.of(o.getStart().getHour(), o.getStart().getMinute())
				.plusMinutes(o.getDuration() * o.getSystemConfig().getDurationUnitInMinutes());
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}
