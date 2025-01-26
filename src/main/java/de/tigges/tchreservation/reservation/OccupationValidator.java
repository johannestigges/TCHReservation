package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OccupationValidator {
    private final OccupationRepository occupationRepository;
    private final Validator validator;

    private static Duration durationUntilStart(Occupation occupation) {
        return Duration.between(LocalDateTime.now(), getStartPoint(occupation));
    }

    private static LocalDateTime getStartPoint(Occupation occupation) {
        return LocalDateTime.of(occupation.getDate(), occupation.getStart());
    }

    public void validateOccupation(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig) {
        validator.clearErrorMessages();

        validateSystemConfigId(occupation);
        validateText(occupation);
        var reservationType = getType(occupation, systemConfig);
        validateStart(occupation, loggedInUser, systemConfig, reservationType);
        validateDuration(occupation, loggedInUser, reservationType);
        validateCourt(occupation, systemConfig);
        validateUserIsActive(loggedInUser);
        validateType(occupation, loggedInUser, systemConfig, reservationType);
        validateOverlap(occupation, systemConfig);

        validator.checkErrorMessages();
    }

    private void validateOverlap(Occupation occupation, ReservationSystemConfig systemConfig) {
        occupationRepository
                .findBySystemConfigIdAndDate(occupation.getSystemConfigId(), occupation.getDate())
                .forEach(occupationEntity -> checkOverlap(occupation, occupationEntity, systemConfig));
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
            throw new BadRequestException(ErrorCode.OCCUPIED, validator.msg(ErrorCode.OCCUPIED,
                    o1.getDate(), o1.getStart(), o1.getCourt()));
        }
    }

    private void validateDuration(Occupation occupation, UserEntity loggedInUser, SystemConfigReservationType type) {
        if (occupation.getDuration() < 1) {
            validator.addFieldErrorMessage("duration", ErrorCode.DURATION_TOO_SMALL);
        }

        if (type.maxDuration() > 0 && occupation.getDuration() > type.maxDuration()) {
            validator.addFieldErrorMessage("duration", ErrorCode.DURATION_TOO_LONG,
                    loggedInUser.getName(), occupation.getDuration());
        }
    }

    private void validateCourt(Occupation occupation, ReservationSystemConfig systemConfig) {
        if (occupation.getCourt() < 1) {
            validator.addFieldErrorMessage("court", ErrorCode.COURT_TOO_SMALL, occupation);
        }
        if (occupation.getCourt() > systemConfig.courts().size()) {
            validator.addFieldErrorMessage("court", ErrorCode.COURT_TOO_BIG,
                    occupation, systemConfig.courts().size());
        }
    }

    private void validateType(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig,
            SystemConfigReservationType type) {
        if (!type.roles().contains(loggedInUser.getRole())) {
            validator.addFieldErrorMessage("type", ErrorCode.USER_CANNOT_ADD_TYPE,
                    loggedInUser.getName(), getTypeName(occupation.getType(), systemConfig.types()));
        }
    }

    private String getTypeName(int type, List<SystemConfigReservationType> types) {
        return types.stream()
                .filter(t -> t.type() == type)
                .map(SystemConfigReservationType::name)
                .findAny()
                .orElseGet(() -> Integer.toString(type));
    }

    private void validateUserIsActive(UserEntity loggedInUser) {
        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(ErrorCode.USER_NOT_ACTIVE,
                    validator.msg(ErrorCode.USER_NOT_ACTIVE, loggedInUser.getName()));
        }
    }

    private SystemConfigReservationType getType(Occupation occupation, ReservationSystemConfig systemConfig) {
        return systemConfig.types().stream()
                .filter(type -> type.type() == occupation.getType())
                .findAny()
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_RESERVATION_TYPE,
                        validator.msg(ErrorCode.INVALID_RESERVATION_TYPE)));
    }

    private void validateSystemConfigId(Occupation occupation) {
        if (occupation.getSystemConfigId() <= 0) {
            throw new BadRequestException(ErrorCode.NO_RESERVATION_SYSTEM,
                    validator.msg(ErrorCode.NO_RESERVATION_SYSTEM));
        }
    }

    private void validateText(Occupation occupation) {
        validator.checkNotEmpty("text", occupation.getText());
    }

    private void validateStart(
            Occupation occupation,
            UserEntity loggedInUser,
            ReservationSystemConfig systemConfig,
            SystemConfigReservationType type) {

        // validate start date and time
        LocalDate date = occupation.getDate();
        if (validator.checkNotEmpty("date", date)) {
            if (type.forbiddenDaysOfWeek().contains(date.getDayOfWeek())) {
                validator.addFieldErrorMessage("date", ErrorCode.DAY_OF_WEEK_NOT_ALLOWED);
            }
        }

        LocalTime start = occupation.getStart();
        validator.checkNotEmpty("start", start);

        if (date != null && start != null) {
            if (start.getHour() < systemConfig.openingHour()) {
                validator.addFieldErrorMessage("start", ErrorCode.START_HOUR_BEFORE_OPENING,
                        start.getHour(), systemConfig.openingHour());
            }

            if (start.getHour() > systemConfig.closingHour()) {
                validator.addFieldErrorMessage("start", ErrorCode.START_HOUR_AFTER_CLOSING,
                        start.getHour(), systemConfig.closingHour());
            }

            if (start.getMinute() != 0 && start.getMinute() % systemConfig.durationUnitInMinutes() != 0) {
                validator.addFieldErrorMessage("start", ErrorCode.START_TIME_MINUTES,
                        start.getMinute());
            }

            if (!loggedInUser.getRole().equals(UserRole.ADMIN) && isOccupationInThePast(occupation)) {
                validator.addFieldErrorMessage("date", ErrorCode.DATE_IN_THE_PAST);
            }
            if (of(date, start.getHour(), start.getMinute())
                    .plusMinutes((long) occupation.getDuration() * systemConfig.durationUnitInMinutes())
                    .isAfter(of(date, systemConfig.closingHour(), 0))) {
                validator.addFieldErrorMessage("start", ErrorCode.START_TIME_PLUS_DURATION);
            }
            if (isOccupationTooFarInFuture(occupation, type)) {
                validator.addFieldErrorMessage("date", ErrorCode.DATE_TOO_FAR_IN_FUTURE);
            }
        }
    }

    private LocalDateTime of(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
    }

    private boolean isOccupationTooFarInFuture(Occupation occupation, SystemConfigReservationType type) {
        var isTooFar = type.maxDaysReservationInFuture() > 0
                && durationUntilStart(occupation).toHours() >= type.maxDaysReservationInFuture() * 24L;
        log.info("is occupation {} at {} too far in future? {} (max days in future: {})",
                occupation.getDate(), occupation.getStart(),
                isTooFar, type.maxDaysReservationInFuture());
        return isTooFar;
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

    private LocalDateTime getStart(Occupation o) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute());
    }

    private LocalDateTime getStart(OccupationEntity o) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute());
    }

    private LocalDateTime getEnd(Occupation o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.durationUnitInMinutes());
    }

    private LocalDateTime getEnd(OccupationEntity o, ReservationSystemConfig systemConfig) {
        return of(o.getDate(), o.getStart().getHour(), o.getStart().getMinute())
                .plusMinutes((long) o.getDuration() * systemConfig.durationUnitInMinutes());
    }
}
