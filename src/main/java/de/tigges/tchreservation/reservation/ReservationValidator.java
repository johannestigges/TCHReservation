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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

        Set<ErrorMessage> errorMessages = new HashSet<>();

        // validate ReservationSystemConfig
        if (occupation.getSystemConfigId() <= 0) {
            throw new BadRequestException(msg("error_no_reservation_system"));
        }
        SystemConfigReservationType type = getType(occupation, systemConfig);

        // validate text
        if (!StringUtils.hasText(occupation.getText())) {
            addOccupationFieldError(errorMessages, "text", msg("error_null_not_allowed"));
        }

        // validate start date and time
        LocalDate date = occupation.getDate();
        if (date == null) {
            addOccupationFieldError(errorMessages, "date", msg("error_null_not_allowed"));
        }

        LocalTime start = occupation.getStart();
        if (start == null) {
            addOccupationFieldError(errorMessages, "start", msg("error_null_not_allowed"));
        }

        if (date != null && start != null) {
            if (start.getHour() < systemConfig.openingHour()) {
                addOccupationFieldError(errorMessages, "start",
                        String.format(msg("error_start_hour_before_opening"),
                                start.getHour(), systemConfig.openingHour()));
            }

            if (start.getHour() > systemConfig.closingHour()) {
                addOccupationFieldError(errorMessages, "start",
                        String.format(msg("error_start_hour_after_closing"),
                                start.getHour(), systemConfig.closingHour()));
            }

            if (start.getMinute() != 0 && start.getMinute() % systemConfig.durationUnitInMinutes() != 0) {
                addOccupationFieldError(errorMessages, "start",
                        String.format(msg("error_start_time_minutes"), start.getMinute()));
            }

            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationInThePast(occupation)) {
                addOccupationFieldError(errorMessages, "date",msg("error_date_in_the_past"));
            }
            if (of(date, start.getHour(), start.getMinute())
                    .plusMinutes((long) occupation.getDuration() * systemConfig.durationUnitInMinutes())
                    .isAfter(of(date, systemConfig.closingHour(), 0))) {
                addOccupationFieldError(errorMessages, "start", msg("error_start_time_plus_duration"));
            }
            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationTooFarInFuture(occupation, type)) {
                addOccupationFieldError(errorMessages, "date", msg("error_date_too_far_in_future"));
            }
        }

        // validate duration
        if (occupation.getDuration() < 1) {
            addOccupationFieldError(errorMessages, "duration", msg("error_duration_too_small"));
        }

        if (type.maxDuration() > 0 && occupation.getDuration() > type.maxDuration()) {
            addOccupationFieldError(errorMessages, "duration", String.format(
                    msg("error_duration_too_long"), loggedInUser.getName(), occupation.getDuration()));
        }

        // validate court
        if (occupation.getCourt() < 1) {
            addOccupationFieldError(errorMessages, "court",
                    String.format(msg("error_court_too_small"), occupation.getCourt()));
        }
        if (occupation.getCourt() > systemConfig.courts().size()) {
            addOccupationFieldError(errorMessages, "court",
                    String.format(msg("error_court_too_big"), occupation.getCourt(), systemConfig.courts().size()));
        }

        // authorization checks
        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(String.format(msg("error_user_not_active"), loggedInUser.getName()));
        }

        if (!type.roles().contains(loggedInUser.getRole())) {
            addOccupationFieldError(errorMessages, "type", String.format(msg("error_user_cannot_add_type"),
                    loggedInUser.getName(), getTypeName(occupation.getType(), systemConfig.types())));
        }


        // check overlap
        occupationRepository.findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
                .forEach(o -> checkOverlap(occupation, o, systemConfig));

        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
        }
    }

    public void validateReservation(Reservation reservation, UserEntity loggedInUser, ReservationSystemConfig systemConfig) {
        Collection<ErrorMessage> errorMessages = new ArrayList<>();

        if (!validateUser(reservation.getUser(), loggedInUser)) {
            throw new AuthorizationException(msg("error_wrong_user"));
        }
        if (!StringUtils.hasText(reservation.getCourts())) {
            addFieldError(errorMessages, "court", msg("error_null_not_allowed"));
        }

        if (ObjectUtils.isEmpty(reservation.getDate())) {
            addFieldError(errorMessages, "date", msg("error_null_not_allowed"));
        }

        if (ObjectUtils.isEmpty(reservation.getStart())) {
            addFieldError(errorMessages, "start", msg("error_null_not_allowed"));
        }

        for (int court : reservation.getCourtsAsArray()) {
            if (court < 1) {
                addFieldError(errorMessages, "court",
                        String.format(msg("error_court_too_small"), court));
            }
            if (court > systemConfig.courts().size()) {
                addFieldError(errorMessages, "court",
                        String.format(msg("error_court_too_big"), court, systemConfig.courts().size()));
            }
        }
        if (RepeatType.daily.equals(reservation.getRepeatType())
                || RepeatType.weekly.equals(reservation.getRepeatType())) {
            if (reservation.getRepeatUntil() == null) {
                addFieldError(errorMessages, "repeatUntil", msg("error_repeatUntil_empty"));
            } else if (reservation.getRepeatUntil().isBefore(reservation.getDate())) {
                addFieldError(errorMessages, "repeatUntil", msg("error_repeatUntil_before_start"));
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
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
        Collection<ErrorMessage> errorMessages = new ArrayList<>();

        for (int i = 0; i < reservation.getOccupations().size(); i++) {
            try {
                validateOccupation(reservation.getOccupations().get(i), loggedInUser, systemConfig);
            } catch (BadRequestException e) {
                errorMessages.addAll(e.getErrorMessages());
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
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

    private void addOccupationFieldError(Collection<ErrorMessage> errorMessages, String field, String message) {
        addFieldError(errorMessages, field, message);
    }

    private void addFieldError(Collection<ErrorMessage> errorMessages, String field, String message) {
        errorMessages.add(new ErrorMessage(message,null, field));
    }

    private LocalDateTime getEnd(Occupation o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.durationUnitInMinutes());
    }

    private LocalDateTime getEnd(OccupationEntity o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.durationUnitInMinutes());
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
        return systemConfig.types().stream()
                .filter(type -> type.type() == occupation.getType())
                .findAny()
                .orElseThrow(() -> new BadRequestException(msg("error_invalid_reservation_type")));
    }
    private String getTypeName(int type, List<SystemConfigReservationType> types) {
        return types.stream()
                .filter(t -> t.type() == type)
                .map(SystemConfigReservationType::name)
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
        if (type.maxDaysReservationInFuture() <=0) {
            return false;
        }
        var start =LocalDateTime.of(occupation.getDate(), occupation.getStart());
        var duration = Duration.between(LocalDateTime.now(), start);
        return duration.toHours() > type.maxDaysReservationInFuture() * 24L;
    }
}
