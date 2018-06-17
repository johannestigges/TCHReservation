package de.tigges.tchreservation.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorDetails;
import de.tigges.tchreservation.exception.FieldError;
import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserAwareService;
import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RestController
@RequestMapping("/reservation")
public class ReservationService extends UserAwareService {

	public static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;
	private ProtocolRepository protocolRepository;
	private MessageSource messageSource;

	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository systemConfigRepository, UserRepository userRepository,
			ProtocolRepository protocolRepository, MessageSource messageSource) {
		super(userRepository);
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = systemConfigRepository;
		this.protocolRepository = protocolRepository;
		this.messageSource = messageSource;
	}

	@PostMapping("/add")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {
		logger.info("add reservation {}", reservation.getText());
		User loggedInUser = getLoggedInUser();

		// check reservation data consistency
		checkReservation(reservation, loggedInUser);

		// create and check Occupations
		List<Occupation> occupations = createOccupations(reservation);
		occupations.forEach(o -> checkOccupation(o, loggedInUser));

		// save occupations and reservation
		Reservation savedReservation = reservationRepository.save(reservation);
		protocolRepository.save(new Protocol(savedReservation, ActionType.CREATE, loggedInUser));
		occupations.forEach(o -> {
			o.setReservation(savedReservation);
			saveOccupation(o, loggedInUser);
		});
		return savedReservation;
	}

	@PutMapping("/update")
	@Transactional
	public void updateReservation(@RequestBody Reservation reservation) {
		logger.info("update reservation {}", reservation.getText());
		Reservation dbReservation = reservationRepository.findById(reservation.getId())
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, reservation.getId()));

		User loggedInUser = getLoggedInUser();

		// check reservation data consistency
		checkReservation(reservation, loggedInUser);
		List<Occupation> occupations = createOccupations(reservation);
		occupations.forEach(o -> checkOccupation(o, loggedInUser));

		reservationRepository.save(reservation);
		protocolRepository.save(new Protocol(reservation, dbReservation, loggedInUser));
		occupationRepository.deleteByReservationId(reservation.getId());
		occupations.forEach(o -> saveOccupation(o, loggedInUser));
	}

	@DeleteMapping("/delete/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable long id) {
		User loggedInUser = getLoggedInUser();
		logger.info("delete reservation {}", id);

		Reservation reservation = reservationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, id));
		occupationRepository.findByReservationId(id).forEach(o -> deleteOccupation(o, loggedInUser));
		reservationRepository.delete(reservation);
		protocolRepository.save(new Protocol(reservation, ActionType.DELETE, loggedInUser));
	}

	/**
	 * get one reservation by key
	 * 
	 * @param reservationId
	 * @return all reservations belonging to that user
	 */
	@GetMapping("/get/{id}")
	public Optional<Reservation> getReservation(@PathVariable long id) {
		logger.info("get reservation ()", id);
		Optional<Reservation> reservation = reservationRepository.findById(id);
		return reservation;
	}

	/**
	 * get all occupations
	 * 
	 * @return list of all occupations for one day
	 */
	@GetMapping("/getOccupations/{systemConfigId}/{date}")
	public Iterable<Occupation> getOccupations(@PathVariable Long systemConfigId, @PathVariable Long date) {
		LocalDate searchDate;
		if (date == null || date.equals(0)) {
			searchDate = LocalDate.now();
		} else {
			searchDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
		}
		logger.info("get occupations date {} ({})", searchDate.toString(), date);
		return occupationRepository.findBySystemConfigIdAndDate(systemConfigId, searchDate);
	}

	/**
	 * get the reservation system configuration
	 * 
	 * @param systemId
	 * @return
	 */
	@GetMapping("/getSystemConfig/{id}")
	public @ResponseBody ReservationSystemConfig getSystemConfig(@RequestParam long id) {
		return systemConfigRepository.get(id);
	}

	private void saveOccupation(Occupation o, User user) {
		Occupation savedOccupation = occupationRepository.save(o);
		protocolRepository.save(new Protocol(savedOccupation, ActionType.CREATE, user));
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
	private void checkReservation(Reservation reservation, User loggedInUser) {

		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		// data consistency checks
		if (reservation == null) {
			throw new BadRequestException(msg("error_no_reservation_data"));
		}

		if (isEmpty(reservation.getText())) {
			addReservationFieldError(errorDetails, "text", msg("error_null_not_allowed"));
		}

		if (reservation.getSystemConfigId() <= 0) {
			throw new BadRequestException(msg("error_no_reservation_system"));
		}

		ReservationSystemConfig systemConfig = systemConfigRepository.get(reservation.getSystemConfigId());

		if (reservation.getUser() == null || reservation.getUser().getId() <= 0) {
			throw new BadRequestException(msg("error_no_user"));
		}
		User user = userRepository.findById(reservation.getUser().getId())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, reservation.getUser().getId()));
		reservation.setUser(user);
		LocalDate date = reservation.getDate();
		if (date == null) {
			addReservationFieldError(errorDetails, "date", msg("error_null_not_allowed"));
			// reservation in the past ist allowed...
			// } else if (date.isBefore(LocalDate.now())) {
			// addReservationFieldError(errorDetails, "date",
			// msg("error_date_in_the_past"));
		}

		LocalTime start = reservation.getStart();
		if (start == null) {
			addReservationFieldError(errorDetails, "start", msg("error_null_not_allowed"));
		}

		if (date != null && start != null) {
			if (date.isEqual(LocalDate.now()) && start.isBefore(LocalTime.now())) {
				addReservationFieldError(errorDetails, "time", msg("error_start_time_in_the_past"));
			}

			if (start.getHour() < systemConfig.getOpeningHour()) {
				addReservationFieldError(errorDetails, "start", String.format(msg("error_start_hour_before_opening"),
						start.getHour(), systemConfig.getOpeningHour()));
			}

			if (start.getHour() > systemConfig.getClosingHour()) {
				addReservationFieldError(errorDetails, "start", String.format(msg("error_start_hour_after_closing"),
						start.getHour(), systemConfig.getClosingHour()));
			}

			if (start.getMinute() != 0 && start.getMinute() % systemConfig.getDurationUnitInMinutes() != 0) {
				addReservationFieldError(errorDetails, "start",
						String.format(msg("error_start_time_minutes"), start.getMinute()));
			}

			if (LocalTime.of(start.getHour(), start.getMinute())
					.plusMinutes(reservation.getDuration() * systemConfig.getDurationUnitInMinutes())
					.isAfter(LocalTime.of(systemConfig.getClosingHour(), 0))) {
				addReservationFieldError(errorDetails, "start", msg("error_start_time_plus_duration"));
			}
		}

		if (reservation.getDuration() < 1) {
			addReservationFieldError(errorDetails, "duration", msg("error_duration_too_small"));
		}

		if (reservation.getCourts() == null) {
			addReservationFieldError(errorDetails, "court", msg("error_null_not_allowed"));
		}

		int[] courts = reservation.courtsArray();

		if (courts.length < 1) {
			addReservationFieldError(errorDetails, "court", msg("error_null_not_allowed"));
		}

		if (courts.length > systemConfig.getCourts()) {
			addReservationFieldError(errorDetails, "court",
					String.format(msg("error_court_too_big"), systemConfig.getCourts()));
		}

		for (int i = 0; i < courts.length; i++) {
			if (courts[i] < 1) {
				addReservationFieldError(errorDetails, "court",
						String.format(msg("error_court_n_too_small"), i, courts[i]));
			}
			if (courts[i] > systemConfig.getCourts()) {
				addReservationFieldError(errorDetails, "court",
						String.format(msg("error_court_n_too_big"), i, courts[i], systemConfig.getCourts()));
			}
		}

		// authorization checks
		if (loggedInUser.hasRole(UserRole.ANONYMOUS)) {
			throw new AuthorizationException(msg("error_anonymous_cannot_add"));
		}

		if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
			throw new AuthorizationException(String.format(msg("error_user_not_active"), loggedInUser.getName()));
		}

		if (loggedInUser.hasRole(UserRole.REGISTERED)) {
			if (!ReservationType.INDIVIDUAL.equals(reservation.getType())) {
				throw new AuthorizationException(String.format(msg("error_registered_cannot_add_type"),
						loggedInUser.getName(), reservation.getType()));
			}
			if (reservation.getDuration() > 3) {
				throw new AuthorizationException(String.format(msg("error_registered_cannot_add_duration"),
						loggedInUser.getName(), reservation.getDuration()));
			}
		}
		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}
	}

	private void deleteOccupation(Occupation occupation, User user) {
		occupationRepository.delete(occupation);
		protocolRepository.save(new Protocol(occupation, ActionType.DELETE, user));
	}

	private void addReservationFieldError(ErrorDetails errorDetails, String field, String message) {
		addFieldError(errorDetails, "reservation", field, message);
	}

	private void addFieldError(ErrorDetails errorDetails, String entity, String field, String message) {
		errorDetails.getFieldErrors().add(new FieldError(entity, field, message));
	}

	/**
	 * create all occupations for a reservation
	 * 
	 * TODO: handle multiple courts
	 * 
	 * TODO: handle repeat weekly until
	 * 
	 * @param reservation
	 * @return list of created occupations
	 */
	private List<Occupation> createOccupations(Reservation reservation) {
		List<Occupation> occupations = new ArrayList<>();
		Occupation occupation = new Occupation();
		occupation.setSystemConfigId(reservation.getSystemConfigId());
		occupation.setReservation(reservation);
		occupation.setText(reservation.getText());
		occupation.setType(reservation.getType());
		occupation.setCourt(reservation.courtsArray()[0]);
		occupation.setLastCourt(occupation.getCourt());
		occupation.setDate(reservation.getDate());
		occupation.setStart(reservation.getStart());
		occupation.setDuration(reservation.getDuration());
		occupations.add(occupation);
		return occupations;
	}

	private void checkOccupation(Occupation occupation, User loggedInUser) {
		occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
				.forEach(o -> checkOverlap(occupation, o));
	}

	private void checkOverlap(Occupation o1, Occupation o2) {
		if (o1.getSystemConfigId() != o2.getSystemConfigId()) {
			return;
		}
		if (o1.getReservation().getId() == o2.getReservation().getId()) {
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
			throw new BadRequestException(
					String.format(msg("error_occupied"), o1.getDate(), o1.getStart(), o1.getCourt()));
		}
	}

	private LocalTime getEnd(Occupation o) {
		ReservationSystemConfig config = systemConfigRepository.get(o.getSystemConfigId());
		return LocalTime.of(o.getStart().getHour(), o.getStart().getMinute())
				.plusMinutes(o.getDuration() * config.getDurationUnitInMinutes());
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	private String msg(String code, Object... args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
}
