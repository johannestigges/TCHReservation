package de.tigges.tchreservation.reservation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RestController
@RequestMapping("/reservation")
public class ReservationService {

	private ReservationRepository reservationRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;
	private UserRepository userRespository;
	private ProtocolRepository protocolRepository;

	public ReservationService(ReservationRepository reservationRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository systemConfigRepository, UserRepository userRepository,
			ProtocolRepository protocolRepository) {
		this.reservationRepository = reservationRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = systemConfigRepository;
		this.userRespository = userRepository;
		this.protocolRepository = protocolRepository;
	}

	@PostMapping("/add")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody Reservation addReservation(@RequestBody Reservation reservation) {

		// check reservation data consistency
		checkReservation(reservation);

		// check Authorization

		// create and check Occupations
		List<Occupation> occupations = createOccupations(reservation);
		occupations.forEach(o -> checkOccupation(o));

		// save occupations and reservation
		Reservation savedReservation = reservationRepository.save(reservation);
		protocolRepository.save(new Protocol(savedReservation, ActionType.CREATE, reservation.getUser()));
		occupations.forEach(o -> {
			o.setReservation(savedReservation);
			saveOccupation(o, reservation.getUser());
		});
		return savedReservation;
	}

	@DeleteMapping("/delete/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable long id) {
		Reservation reservation = reservationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.RESERVATION, id));
		occupationRepository.findByReservationId(id).forEach(o -> deleteOccupation(o, reservation.getUser()));
		reservationRepository.delete(reservation);
		protocolRepository.save(new Protocol(reservation, ActionType.DELETE, reservation.getUser()));
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
	private void checkReservation(Reservation reservation) {

		ErrorDetails errorDetails = new ErrorDetails("error validation reservation", null);

		// data consistency checks
		if (reservation == null) {
			throw new BadRequestException("no reservation data");
		}

		if (isEmpty(reservation.getText())) {
			addReservationFieldError(errorDetails, "text", "null value not allowed");
		}

		if (reservation.getSystemConfigId() <= 0) {
			throw new BadRequestException("no reservation system");
		}

		ReservationSystemConfig systemConfig = systemConfigRepository.get(reservation.getSystemConfigId());

		if (reservation.getUser() == null || reservation.getUser().getId() <= 0) {
			throw new BadRequestException("no user");
		}
		User user = userRespository.findById(reservation.getUser().getId())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, reservation.getUser().getId()));
		reservation.setUser(user);

		LocalDate date = reservation.getDate();
		if (date == null) {
			addReservationFieldError(errorDetails, "date", "null value not allowed");
		} else if (date.isBefore(LocalDate.now())) {
			addReservationFieldError(errorDetails, "date", "date in the past is not allowed");
		}

		LocalTime start = reservation.getStart();
		if (start == null) {
			addReservationFieldError(errorDetails, "start", "null value not allowed");
		}

		if (date != null && start != null) {
			if (date.isEqual(LocalDate.now()) && start.isBefore(LocalTime.now())) {
				addReservationFieldError(errorDetails, "time", "start time in the past is not allowed");
			}

			if (start.getHour() < systemConfig.getOpeningHour()) {
				addReservationFieldError(errorDetails, "start",
						String.format("start hour %02d:00 before opening hour %02d:00 not allowed", start.getHour(),
								systemConfig.getOpeningHour()));
			}

			if (start.getHour() > systemConfig.getClosingHour()) {
				addReservationFieldError(errorDetails, "start",
						String.format("start hour %02d:00 after closing hour %02d:00 not allowed", //
								start.getHour(), systemConfig.getClosingHour()));
			}

			if (start.getMinute() != 0 && start.getMinute() % systemConfig.getDurationUnitInMinutes() != 0) {
				addReservationFieldError(errorDetails, "start",
						String.format("start time with %d minutes not allowed", start.getMinute()));
			}

			if (LocalTime.of(start.getHour(), start.getMinute())
					.plusMinutes(reservation.getDuration() * systemConfig.getDurationUnitInMinutes())
					.isAfter(LocalTime.of(systemConfig.getClosingHour(), 0))) {
				addReservationFieldError(errorDetails, "start", "starttime plus duration greater than closing hour.");
			}
		}

		if (reservation.getDuration() < 1) {
			addReservationFieldError(errorDetails, "duration", "duration must be greater than 0");
		}

		if (reservation.getCourts() == null) {
			addReservationFieldError(errorDetails, "court", "null value not allowed");
		}

		int[] courts = reservation.courtsArray();

		if (courts.length < 1) {
			addReservationFieldError(errorDetails, "court", "null value not allowed");
		}

		if (courts.length > systemConfig.getCourts()) {
			addReservationFieldError(errorDetails, "court",
					String.format("more than %d courts not allowed", systemConfig.getCourts()));
		}

		for (int i = 0; i < courts.length; i++) {
			if (courts[i] < 1) {
				addReservationFieldError(errorDetails, "court",
						String.format("court[%d]: %d < 1 not allowed", i, courts[i]));
			}
			if (courts[i] > systemConfig.getCourts()) {
				addReservationFieldError(errorDetails, "court",
						String.format("court[%d]: %d > %d not allowed", i, courts[i], systemConfig.getCourts()));
			}
		}

		// authorization checks
		if (user.hasRole(UserRole.ANONYMOUS)) {
			throw new AuthorizationException("user with role ANONYMOUS cannot add reservation.");
		}

		if (!ActivationStatus.ACTIVE.equals(user.getStatus())) {
			throw new AuthorizationException(String.format("user %s is not active.", user.getName()));
		}

		if (user.hasRole(UserRole.REGISTERED)) {
			if (!ReservationType.INDIVIDUAL.equals(reservation.getType())) {
				throw new AuthorizationException(
						String.format("user %s with role REGISTERED cannot add reservation of type %s.", user.getName(),
								reservation.getType()));
			}
			if (reservation.getDuration() > 3) {
				throw new AuthorizationException(
						String.format("user %s with role REGISTERED cannot add reservation with duration %d.",
								user.getName(), reservation.getDuration()));
			}
		}
		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}
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
		System.out.println(String.format("search for system config %d and date %s", systemConfigId, searchDate.toString()));
		return occupationRepository.findBySystemConfigIdAndDate(systemConfigId, searchDate);
	}

	/**
	 * get all reservations for one user
	 * 
	 * @param userId
	 * @return all reservations belonging to that user
	 */
	@GetMapping("/get/{user}")
	public Iterable<Reservation> getReservations(@PathVariable long userId) {
		return reservationRepository.findByUserId(userId);
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

	private void checkOccupation(Occupation occupation) {
		occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
				.forEach(o -> checkOverlap(occupation, o));
	}

	private void checkOverlap(Occupation o1, Occupation o2) {
		if (o1.getSystemConfigId() != o2.getSystemConfigId()) {
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
					String.format("cannot add occupation on %tF %tR becauce court %d ist occupied.", o1.getDate(),
							o1.getStart(), o1.getCourt()));
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
}
