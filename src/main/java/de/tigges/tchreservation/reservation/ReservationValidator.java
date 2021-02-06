package de.tigges.tchreservation.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorDetails;
import de.tigges.tchreservation.exception.FieldError;
import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

	private final OccupationRepository occupationRepository;
	private final ReservationSystemConfigRepository systemConfigRepository;
	private final MessageSource messageSource;

	/**
	 * validate an occupation
	 * <p>
	 * <li>data consistency checks
	 * <li>authorization checks
	 * <li>occupation overlap checks
	 * 
	 * @param occupation
	 * @param loggedInUser
	 */
	public void validateOccupation(Occupation occupation, UserEntity loggedInUser) {

		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		// validate ReservationSystemConfig
		if (occupation.getSystemConfigId() <= 0) {
			throw new BadRequestException(msg("error_no_reservation_system"));
		}
		ReservationSystemConfig systemConfig = systemConfigRepository.get(occupation.getSystemConfigId());

		// validate text
		if (StringUtils.isEmpty(occupation.getText())) {
			addOccupationFieldError(errorDetails, "text", msg("error_null_not_allowed"));
		}

		// validate start date and time
		LocalDate date = occupation.getDate();
		if (date == null) {
			addOccupationFieldError(errorDetails, "date", msg("error_null_not_allowed"));
		}

		LocalTime start = occupation.getStart();
		if (start == null) {
			addOccupationFieldError(errorDetails, "start", msg("error_null_not_allowed"));
		}

		if (date != null && start != null) {
			if (start.getHour() < systemConfig.getOpeningHour()) {
				addOccupationFieldError(errorDetails, "start", String.format(msg("error_start_hour_before_opening"),
						start.getHour(), systemConfig.getOpeningHour()));
			}

			if (start.getHour() > systemConfig.getClosingHour()) {
				addOccupationFieldError(errorDetails, "start", String.format(msg("error_start_hour_after_closing"),
						start.getHour(), systemConfig.getClosingHour()));
			}

			if (start.getMinute() != 0 && start.getMinute() % systemConfig.getDurationUnitInMinutes() != 0) {
				addOccupationFieldError(errorDetails, "start",
						String.format(msg("error_start_time_minutes"), start.getMinute()));
			}

			if (LocalTime.of(start.getHour(), start.getMinute())
					.plusMinutes(occupation.getDuration() * systemConfig.getDurationUnitInMinutes())
					.isAfter(LocalTime.of(systemConfig.getClosingHour(), 0))) {
				addOccupationFieldError(errorDetails, "start", msg("error_start_time_plus_duration"));
			}
		}

		// validate duration
		if (occupation.getDuration() < 1) {
			addOccupationFieldError(errorDetails, "duration", msg("error_duration_too_small"));
		}

		// validate court
		if (occupation.getCourt() < 1) {
			addOccupationFieldError(errorDetails, "court",
					String.format(msg("error_court_too_small"), occupation.getCourt()));
		}
		if (occupation.getCourt() > systemConfig.getCourts()) {
			addOccupationFieldError(errorDetails, "court",
					String.format(msg("error_court_too_big"), occupation.getCourt(), systemConfig.getCourts()));
		}

		// authorization checks
		if (UserUtils.hasRole(loggedInUser.getRole(), UserRole.ANONYMOUS)) {
			throw new AuthorizationException(msg("error_anonymous_cannot_add"));
		}

		if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
			throw new AuthorizationException(String.format(msg("error_user_not_active"), loggedInUser.getName()));
		}

		// user role checks
		if (UserUtils.hasRole(loggedInUser.getRole(), UserRole.REGISTERED, UserRole.KIOSK, UserRole.TECHNICAL)) {
			// only reservation type individual allowed
			if (!ReservationType.INDIVIDUAL.equals(occupation.getType())) {
				addOccupationFieldError(errorDetails, "type", String.format(msg("error_registered_cannot_add_type"),
						loggedInUser.getName(), occupation.getType()));
			}

			// only duration <=3 is allowed
			if (occupation.getDuration() > 3) {
				addOccupationFieldError(errorDetails, "duration", String.format(
						msg("error_registered_cannot_add_duration"), loggedInUser.getName(), occupation.getDuration()));
			}

			// occupation in the past is not allowed
			if (date.isBefore(LocalDate.now())) {
				addOccupationFieldError(errorDetails, "date", msg("error_date_in_the_past"));
			}
			if (date.isEqual(LocalDate.now()) && start.isBefore(LocalTime.now().minusMinutes(60))) {
				addOccupationFieldError(errorDetails, "time", msg("error_start_time_in_the_past"));
			}

			// reservation only this and next day is allowed
			if (date.isAfter(LocalDate.now().plusDays(1))) {
				addOccupationFieldError(errorDetails, "date", msg("error_date_too_far_in_future"));
			}
		}

		// check overlap
		occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
				.forEach(o -> checkOverlap(occupation, o));

		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}
	}

	public void validateReservation(Reservation reservation, UserEntity loggedInUser) {
		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		ReservationSystemConfig systemConfig = systemConfigRepository.get(reservation.getSystemConfigId());

		if (StringUtils.isEmpty(reservation.getCourts())) {
			addFieldError(errorDetails, "reservation", "court", msg("error_null_not_allowed"));
		}

		if (StringUtils.isEmpty(reservation.getDate())) {
			addFieldError(errorDetails, "reservation", "date", msg("error_null_not_allowed"));
		}

		if (StringUtils.isEmpty(reservation.getStart())) {
			addFieldError(errorDetails, "reservation", "start", msg("error_null_not_allowed"));
		}

		for (int court : reservation.getCourtsAsArray()) {
			if (court < 1) {
				addFieldError(errorDetails, "reservation", "court", String.format(msg("error_court_too_small"), court));
			}
			if (court > systemConfig.getCourts()) {
				addFieldError(errorDetails, "reservation", "court",
						String.format(msg("error_court_too_big"), court, systemConfig.getCourts()));

			}
		}

		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}

	}

	public void validateOccupations(Reservation reservation, UserEntity loggedInUser) {
		ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

		for (int i = 0; i < reservation.getOccupations().size(); i++) {
			try {
				validateOccupation(reservation.getOccupations().get(i), loggedInUser);
			} catch (BadRequestException e) {
				if (e.getErrorDetails().getFieldErrors().isEmpty()) {
					addFieldError(errorDetails, "occupation[" + i + "]", "occupation",
							e.getErrorDetails().getMessage());
				}
				for (FieldError fe : e.getErrorDetails().getFieldErrors()) {
					addFieldError(errorDetails, "occupation[" + i + "]", fe.getField(), fe.getMessage());
				}
			}
		}
		if (!errorDetails.getFieldErrors().isEmpty()) {
			throw new InvalidDataException(errorDetails);
		}
	}

	public void validateOverlap(Occupation occupation, UserEntity loggedInUser) {

	}

	private void checkOverlap(Occupation o1, OccupationEntity o2) {
		if (o1.getId() == o2.getId()) {
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

	private void addOccupationFieldError(ErrorDetails errorDetails, String field, String message) {
		addFieldError(errorDetails, "occupation", field, message);
	}

	private void addFieldError(ErrorDetails errorDetails, String entity, String field, String message) {
		errorDetails.getFieldErrors().add(new FieldError(entity, field, message));
	}

	private LocalTime getEnd(Occupation o) {
		ReservationSystemConfig config = systemConfigRepository.get(o.getSystemConfigId());
		return LocalTime.of(o.getStart().getHour(), o.getStart().getMinute())
				.plusMinutes(o.getDuration() * config.getDurationUnitInMinutes());
	}

	private LocalTime getEnd(OccupationEntity o) {
		ReservationSystemConfig config = systemConfigRepository.get(o.getSystemConfigId());
		return LocalTime.of(o.getStart().getHour(), o.getStart().getMinute())
				.plusMinutes(o.getDuration() * config.getDurationUnitInMinutes());
	}

	private String msg(String code, Object... args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}
}
