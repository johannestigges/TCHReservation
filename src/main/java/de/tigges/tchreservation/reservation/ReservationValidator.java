package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.*;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationValidator {

    private final OccupationRepository occupationRepository;
    private final Validator validator;

    private static LocalDateTime getStartPoint(Occupation occupation) {
        return LocalDateTime.of(occupation.getDate(), occupation.getStart());
    }

    private static Duration durationUntilStart(Occupation occupation) {
        return Duration.between(LocalDateTime.now(), getStartPoint(occupation));
    }

    public void validateOccupation(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {

        validator.startValidation();

        validateSystemConfigId(occupation);
        var reservationType = getType(occupation, systemConfig);
        validateText(occupation);
        validateStart(occupation, loggedInUser, systemConfig, reservationType);
        validateDuration(occupation, loggedInUser, reservationType);
        validateCourt(occupation, systemConfig);
        validateUserIsActive(loggedInUser);
        validateType(occupation, loggedInUser, systemConfig, reservationType);
        validateOverlap(occupation, systemConfig);

        validator.checkErrorMessages();
    }

    private void validateType(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig,
            SystemConfigReservationType type) {
        if (!type.roles().contains(loggedInUser.getRole())) {
            validator.addFieldErrorMessage("type", "error_user_cannot_add_type",
                    loggedInUser.getName(), getTypeName(occupation.getType(), systemConfig.types()));
        }
    }

    private void validateOverlap(Occupation occupation, ReservationSystemConfig systemConfig) {
        occupationRepository
                .findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
                .forEach(occupationEntity -> checkOverlap(occupation, occupationEntity, systemConfig));
    }

    private void validateUserIsActive(UserEntity loggedInUser) {
        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(validator.msg("error_user_not_active", loggedInUser.getName()));
        }
    }

    private void validateCourt(Occupation occupation, ReservationSystemConfig systemConfig) {
        if (occupation.getCourt() < 1) {
            validator.addFieldErrorMessage("court", "error_court_too_small", occupation);
        }
        if (occupation.getCourt() > systemConfig.courts().size()) {
            validator.addFieldErrorMessage("court", "error_court_too_big",
                    occupation, systemConfig.courts().size());
        }
    }

    private void validateDuration(Occupation occupation, UserEntity loggedInUser, SystemConfigReservationType type) {
        if (occupation.getDuration() < 1) {
            validator.addFieldErrorMessage("duration", "error_duration_too_small");
        }

        if (type.maxDuration() > 0 && occupation.getDuration() > type.maxDuration()) {
            validator.addFieldErrorMessage("duration", "error_duration_too_long",
                    loggedInUser.getName(), occupation.getDuration());
        }
    }

    private void validateStart(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig,
            SystemConfigReservationType type) {

        // validate start date and time
        LocalDate date = occupation.getDate();
        if (date == null) {
            validator.addFieldErrorMessage("date", "error_null_not_allowed");
        } else if (type.forbiddenDaysOfWeek().contains(date.getDayOfWeek())) {
            validator.addFieldErrorMessage("date", "error_day_of_week_not_allowed");
        }

        LocalTime start = occupation.getStart();
        if (start == null) {
            validator.addFieldErrorMessage("start", "error_null_not_allowed");
        }

        if (date != null && start != null) {
            if (start.getHour() < systemConfig.openingHour()) {
                validator.addFieldErrorMessage("start", "error_start_hour_before_opening",
                        start.getHour(), systemConfig.openingHour());
            }

            if (start.getHour() > systemConfig.closingHour()) {
                validator.addFieldErrorMessage("start", "error_start_hour_after_closing",
                        start.getHour(), systemConfig.closingHour());
            }

            if (start.getMinute() != 0 && start.getMinute() % systemConfig.durationUnitInMinutes() != 0) {
                validator.addFieldErrorMessage("start", "error_start_time_minutes",
                        start.getMinute());
            }

            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationInThePast(occupation)) {
                validator.addFieldErrorMessage("date", "error_date_in_the_past");
            }
            if (of(date, start.getHour(), start.getMinute())
                    .plusMinutes((long) occupation.getDuration() * systemConfig.durationUnitInMinutes())
                    .isAfter(of(date, systemConfig.closingHour(), 0))) {
                validator.addFieldErrorMessage("start", "error_start_time_plus_duration");
            }
            if (isOccupationTooFarInFuture(occupation, type)) {
                validator.addFieldErrorMessage("date", "error_date_too_far_in_future");
            }
        }
    }

    private void validateText(Occupation occupation) {
        if (!StringUtils.hasText(occupation.getText())) {
            validator.addFieldErrorMessage("text", "error_null_not_allowed");
        }
    }

    private void validateSystemConfigId(Occupation occupation) {
        if (occupation.getSystemConfigId() <= 0) {
            throw new BadRequestException(validator.msg("error_no_reservation_system"));
        }
    }

    public void validateReservation(
            Reservation reservation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {

        validator.startValidation();

        if (!checkUser(reservation.getUser(), loggedInUser)) {
            throw new AuthorizationException(validator.msg("error_wrong_user"));
        }
        if (!StringUtils.hasText(reservation.getCourts())) {
            validator.addFieldErrorMessage("court", "error_null_not_allowed");
        }

        if (ObjectUtils.isEmpty(reservation.getDate())) {
            validator.addFieldErrorMessage("date", "error_null_not_allowed");
        }

        if (ObjectUtils.isEmpty(reservation.getStart())) {
            validator.addFieldErrorMessage("start", "error_null_not_allowed");
        }

        for (int court : reservation.getCourtsAsArray()) {
            if (court < 1) {
                validator.addFieldErrorMessage("court",
                        "error_court_too_small", court);
            }
            if (court > systemConfig.courts().size()) {
                validator.addFieldErrorMessage("court",
                        "error_court_too_big", court, systemConfig.courts().size());
            }
        }
        if (RepeatType.daily.equals(reservation.getRepeatType())
                || RepeatType.weekly.equals(reservation.getRepeatType())) {
            if (reservation.getRepeatUntil() == null) {
                validator.addFieldErrorMessage("repeatUntil", "error_repeatUntil_empty");
            } else if (reservation.getRepeatUntil().isBefore(reservation.getDate())) {
                validator.addFieldErrorMessage("repeatUntil", "error_repeatUntil_before_start");
            }
        }

        validator.checkErrorMessages();
    }

    private boolean checkUser(User user, UserEntity loggedInUser) {
        if (user == null) {
            return true;
        }
        return user.getName().equals(loggedInUser.getName())
                && user.getStatus().equals(loggedInUser.getStatus())
                && user.getRole().equals(loggedInUser.getRole());
    }

    public void validateOccupations(
            Reservation reservation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {

        validator.startValidation();

        for (int i = 0; i < reservation.getOccupations().size(); i++) {
            try {
                validateOccupation(reservation.getOccupations().get(i), loggedInUser, systemConfig);
            } catch (BadRequestException e) {
                validator.addErrorMessages(e.getErrorMessages());
            }
        }
        validator.checkErrorMessages();
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
            throw new BadRequestException(validator.msg("error_occupied",
                    o1.getDate(), o1.getStart(), o1.getCourt()));
        }
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

    private SystemConfigReservationType getType(Occupation occupation, ReservationSystemConfig systemConfig) {
        return systemConfig.types().stream()
                .filter(type -> type.type() == occupation.getType())
                .findAny()
                .orElseThrow(() -> new BadRequestException(validator.msg("error_invalid_reservation_type")));
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
        } else {
            return occupation.getStart().getHour() < LocalTime.now().getHour();
        }
    }

    private boolean isOccupationTooFarInFuture(Occupation occupation, SystemConfigReservationType type) {
        var isTooFar = type.maxDaysReservationInFuture() > 0
                && durationUntilStart(occupation).toHours() >= type.maxDaysReservationInFuture() * 24L;
        log.info("is occupation {} at {} too far in future? {} (max days in future: {})",
                occupation.getDate(), occupation.getStart(),
                isTooFar, type.maxDaysReservationInFuture());
        return isTooFar;
    }
}
