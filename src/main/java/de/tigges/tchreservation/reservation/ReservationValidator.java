package de.tigges.tchreservation.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorDetails;
import de.tigges.tchreservation.exception.FieldError;
import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@Component
public class ReservationValidator {

	private UserRepository userRepository;
	private OccupationRepository occupationRepository;
	private ReservationSystemConfigRepository systemConfigRepository;
	private MessageSource messageSource;

	public ReservationValidator(UserRepository userRepository, OccupationRepository occupationRepository,
			ReservationSystemConfigRepository reservationSystemConfigRepository, MessageSource messageSource) {
		this.userRepository = userRepository;
		this.occupationRepository = occupationRepository;
		this.systemConfigRepository = reservationSystemConfigRepository;
		this.messageSource = messageSource;
	}

	/**
	 * validate a reservation
	 * <p>
	 * <li>data consistency checks
	 * <li>authorization checks
	 * <li>occupation overlap checks
	 * 
	 * @param reservation
	 * @param loggedInUser
	 */
	public void validateReservation(Reservation reservation, User loggedInUser) {
		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		// validate ReservationSystemConfig
		if (reservation.getSystemConfigId() <= 0) {
			throw new BadRequestException(msg("error_no_reservation_system"));
		}
		ReservationSystemConfig systemConfig = systemConfigRepository.get(reservation.getSystemConfigId());

		// validate user
		if (reservation.getUser() == null || reservation.getUser().getId() <= 0) {
			throw new BadRequestException(msg("error_no_user"));
		}
		User user = userRepository.findById(reservation.getUser().getId())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, reservation.getUser().getId()));
		reservation.setUser(user);

		// validate text
		if (isEmpty(reservation.getText())) {
			addReservationFieldError(errorDetails, "text", msg("error_null_not_allowed"));
		}

		// validate start date and time
		LocalDate date = reservation.getDate();
		if (date == null) {
			addReservationFieldError(errorDetails, "date", msg("error_null_not_allowed"));
		}

		LocalTime start = reservation.getStart();
		if (start == null) {
			addReservationFieldError(errorDetails, "start", msg("error_null_not_allowed"));
		}

		if (date != null && start != null) {
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

		// validate duration
		if (reservation.getDuration() < 1) {
			addReservationFieldError(errorDetails, "duration", msg("error_duration_too_small"));
		}

		// validate courts
		if (isEmpty(reservation.getCourts())) {
			addReservationFieldError(errorDetails, "court", msg("error_null_not_allowed"));
		}

		int[] courts = reservation.getCourtsAsArray();

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
						String.format(msg("error_court_n_too_small"), i + 1, courts[i]));
			}
			if (courts[i] > systemConfig.getCourts()) {
				addReservationFieldError(errorDetails, "court",
						String.format(msg("error_court_n_too_big"), i + 1, courts[i], systemConfig.getCourts()));
			}
		}

		// authorization checks
		if (loggedInUser.hasRole(UserRole.ANONYMOUS)) {
			throw new AuthorizationException(msg("error_anonymous_cannot_add"));
		}

		if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
			throw new AuthorizationException(String.format(msg("error_user_not_active"), loggedInUser.getName()));
		}

		if (loggedInUser.hasRole(UserRole.REGISTERED, UserRole.KIOSK)) {
			// only reservation type individual allowed
			if (!ReservationType.INDIVIDUAL.equals(reservation.getType())) {
				addReservationFieldError(errorDetails, "type", String.format(msg("error_registered_cannot_add_type"),
						loggedInUser.getName(), reservation.getType()));
			}

			// only duration <=3 is allowed
			if (reservation.getDuration() > 3) {
				addReservationFieldError(errorDetails, "duration",
						String.format(msg("error_registered_cannot_add_duration"), loggedInUser.getName(),
								reservation.getDuration()));
			}

			// reservation in the past is not allowed
			if (date.isBefore(LocalDate.now())) {
				addReservationFieldError(errorDetails, "date", msg("error_date_in_the_past"));
			}
			if (date.isEqual(LocalDate.now()) && start.isBefore(LocalTime.now())) {
				addReservationFieldError(errorDetails, "time", msg("error_start_time_in_the_past"));
			}
		}

		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}
	}

	public void validateOccupations(Reservation reservation, User loggedInUser) {
		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		for (int i = 0; i < reservation.getOccupations().size(); i++) {
			try {
				validateOccupation(reservation, reservation.getOccupations().get(i), loggedInUser);
			} catch (BadRequestException e) {
				addReservationFieldError(errorDetails, "occupation[" + i + "]", e.getErrorDetails().getMessage());
			}
		}
		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}

	}

	public void validateOccupation(Reservation reservation, Occupation occupation, User loggedInUser) {
		if (reservation.getSystemConfigId() != occupation.getSystemConfigId()) {
			throw new BadRequestException("system config of occupation doesn't match");
		}

		occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
				.forEach(o -> checkOverlap(occupation, o));
	}

	private void checkOverlap(Occupation o1, Occupation o2) {
		if (o1.getReservation().getId() == o2.getReservation().getId()) {
			return;
		}
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
					String.format(msg("error_occupied"), o1.getDate(), o1.getStart(), o1.getCourt()));
		}
	}

	private void addReservationFieldError(ErrorDetails errorDetails, String field, String message) {
		errorDetails.getFieldErrors().add(new FieldError("reservation", field, message));
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
