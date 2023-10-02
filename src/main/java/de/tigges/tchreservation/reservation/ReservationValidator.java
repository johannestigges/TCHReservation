package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.*;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.*;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final OccupationRepository occupationRepository;
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
    public void validateOccupation(Occupation occupation, UserEntity loggedInUser, ReservationSystemConfig systemConfig) {

        ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

        // validate ReservationSystemConfig
        if (occupation.getSystemConfigId() <= 0) {
            throw new BadRequestException(msg("error_no_reservation_system"));
        }
        SystemConfigReservationType type = getType(occupation, systemConfig);

        // validate text
        if (!StringUtils.hasText(occupation.getText())) {
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
                addOccupationFieldError(errorDetails, "start",
                        String.format(msg("error_start_hour_before_opening"),
                                start.getHour(), systemConfig.getOpeningHour()));
            }

            if (start.getHour() > systemConfig.getClosingHour()) {
                addOccupationFieldError(errorDetails, "start",
                        String.format(msg("error_start_hour_after_closing"),
                                start.getHour(), systemConfig.getClosingHour()));
            }

            if (start.getMinute() != 0 && start.getMinute() % systemConfig.getDurationUnitInMinutes() != 0) {
                addOccupationFieldError(errorDetails, "start",
                        String.format(msg("error_start_time_minutes"), start.getMinute()));
            }

            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationInThePast(occupation)) {
                addOccupationFieldError(errorDetails, "date",msg("error_date_in_the_past"));
            }
            if (of(date, start.getHour(), start.getMinute())
                    .plusMinutes((long) occupation.getDuration() * systemConfig.getDurationUnitInMinutes())
                    .isAfter(of(date, systemConfig.getClosingHour(), 0))) {
                addOccupationFieldError(errorDetails, "start", msg("error_start_time_plus_duration"));
            }
            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationTooFarInFuture(occupation, type)) {
                addOccupationFieldError(errorDetails, "date", msg("error_date_too_far_in_future"));
            }
        }

        // validate duration
        if (occupation.getDuration() < 1) {
            addOccupationFieldError(errorDetails, "duration", msg("error_duration_too_small"));
        }

        if (type.getMaxDuration() > 0 && occupation.getDuration() > type.getMaxDuration()) {
            addOccupationFieldError(errorDetails, "duration", String.format(
                    msg("error_duration_too_long"), loggedInUser.getName(), occupation.getDuration()));
        }

        // validate court
        if (occupation.getCourt() < 1) {
            addOccupationFieldError(errorDetails, "court",
                    String.format(msg("error_court_too_small"), occupation.getCourt()));
        }
        if (occupation.getCourt() > systemConfig.getCourts().size()) {
            addOccupationFieldError(errorDetails, "court",
                    String.format(msg("error_court_too_big"), occupation.getCourt(), systemConfig.getCourts().size()));
        }

        // authorization checks
        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(String.format(msg("error_user_not_active"), loggedInUser.getName()));
        }

        if (!type.getRoles().contains(loggedInUser.getRole())) {
            addOccupationFieldError(errorDetails, "type", String.format(msg("error_user_cannot_add_type"),
                    loggedInUser.getName(), getTypeName(occupation.getType(), systemConfig.getTypes())));
        }


        // check overlap
        occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
                .forEach(o -> checkOverlap(occupation, o, systemConfig));

        if (!errorDetails.getFieldErrors().isEmpty()) {
            throw new InvalidDataException(errorDetails);
        }
    }

    public void validateReservation(Reservation reservation, UserEntity loggedInUser, ReservationSystemConfig systemConfig) {
        ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

        if (!validateUser(reservation.getUser(), loggedInUser)) {
            throw new AuthorizationException(msg("error_wrong_user"));
        }
        if (!StringUtils.hasText(reservation.getCourts())) {
            addFieldError(errorDetails, "reservation", "court", msg("error_null_not_allowed"));
        }

        if (ObjectUtils.isEmpty(reservation.getDate())) {
            addFieldError(errorDetails, "reservation", "date", msg("error_null_not_allowed"));
        }

        if (ObjectUtils.isEmpty(reservation.getStart())) {
            addFieldError(errorDetails, "reservation", "start", msg("error_null_not_allowed"));
        }

        for (int court : reservation.getCourtsAsArray()) {
            if (court < 1) {
                addFieldError(errorDetails, "reservation", "court",
                        String.format(msg("error_court_too_small"), court));
            }
            if (court > systemConfig.getCourts().size()) {
                addFieldError(errorDetails, "reservation", "court",
                        String.format(msg("error_court_too_big"), court, systemConfig.getCourts().size()));
            }
        }
        if (RepeatType.daily.equals(reservation.getRepeatType())
                || RepeatType.weekly.equals(reservation.getRepeatType())) {
            if (reservation.getRepeatUntil() == null) {
                addFieldError(errorDetails, "reservation", "repeatUntil", msg("error_repeatUntil_empty"));
            } else if (reservation.getRepeatUntil().isBefore(reservation.getDate())) {
                addFieldError(errorDetails, "reservation", "repeatUntil", msg("error_repeatUntil_before_start"));
            }
        }

        if (!errorDetails.getFieldErrors().isEmpty()) {
            throw new InvalidDataException(errorDetails);
        }
    }

    private boolean validateUser(User user, UserEntity loggedInUser) {
        if (user == null) {
            return true;
        }
        return user.getName().equals(loggedInUser.getName())
                && user.getStatus().equals(loggedInUser.getStatus())
                && user.getRole().equals(loggedInUser.getRole());
    }

    public void validateOccupations(Reservation reservation, UserEntity loggedInUser, ReservationSystemConfig systemConfig) {
        ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_reservation"), null);

        for (int i = 0; i < reservation.getOccupations().size(); i++) {
            try {
                validateOccupation(reservation.getOccupations().get(i), loggedInUser, systemConfig);
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

    private void checkOverlap(Occupation o1, OccupationEntity o2, ReservationSystemConfig systemConfig) {
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
        if (getStart(o1).isBefore(getEnd(o2, systemConfig)) && getEnd(o1, systemConfig).isAfter(getStart(o2))) {
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

    private LocalDateTime getEnd(Occupation o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.getDurationUnitInMinutes());
    }

    private LocalDateTime getEnd(OccupationEntity o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.getDurationUnitInMinutes());
    }

    private LocalDateTime of(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
    }

    private LocalDateTime getStart(Occupation o) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute());
    }

    private LocalDateTime getStart(OccupationEntity o) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute());
    }


    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }

    private SystemConfigReservationType getType(Occupation occupation, ReservationSystemConfig systemConfig) {
        return systemConfig.getTypes().stream()
                .filter(type -> type.getType() == occupation.getType())
                .findAny()
                .orElseThrow(() -> new BadRequestException(msg("error_invalid_reservation_type")));
    }
    private String getTypeName(int type, List<SystemConfigReservationType> types) {
        return types.stream()
                .filter(t -> t.getType() == type)
                .map(SystemConfigReservationType::getName)
                .findAny()
                .orElseGet(() -> Integer.toString(type));
    }

    private boolean isOccupationInThePast(Occupation occupation) {
        var today = LocalDate.now();
        if (occupation.getDate().isBefore(today)) {
            return true;
        } else if (occupation.getDate().isAfter(today)) {
            return false;
        } else return occupation.getStart().getHour() < LocalTime.now().getHour();
    }
    private boolean isOccupationTooFarInFuture(Occupation occupation, SystemConfigReservationType type) {
        if (type.getMaxDaysReservationInFuture() <=0) {
            return false;
        }
        var start =LocalDateTime.of(occupation.getDate(), occupation.getStart());
        var duration = Duration.between(LocalDateTime.now(), start);
        return duration.toHours() > type.getMaxDaysReservationInFuture() * 24L;
    }
}
