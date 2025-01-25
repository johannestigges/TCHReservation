package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
public class SystemConfigValidator {

    private static final int MAX_COURTS = 20;
    private static final int MIN_STRING_LENGTH = 3;
    private static final int MAX_STRING_LENGTH = 50;

    private final Validator validator;

    public void validate(ReservationSystemConfig config, UserEntity loggedInUser) {
        validator.clearErrorMessages();
        checkConfigId(config);
        checkUser(loggedInUser);
        checkString("name", config.name());
        checkCourts(config);
        validator.checkInt("durationUnitInMinutes", config.durationUnitInMinutes(), 30, 60);
        validator.checkInt("maxDaysReservationInFuture", config.maxDaysReservationInFuture(), 1, 365);
        validator.checkInt("maxDuration", config.maxDuration(), 1, 20);
        checkOpeningAndClosingHour(config);
        checkTypes(config);
        validator.checkErrorMessages();
    }

    private void checkCourts(ReservationSystemConfig config) {
        if (validator.checkNotEmpty("courts", config.courts())) {
            if (config.courts().size() > MAX_COURTS) {
                validator.addFieldErrorMessage("courts", ErrorCode.TOO_MANY_COURTS, "error_too_many_courts");
            }
            config.courts().forEach(court -> checkString("court", court));
        }
    }

    private void checkConfigId(ReservationSystemConfig config) {
        if (config.id() < 1) {
            throw new BadRequestException(ErrorCode.NO_SYSTEM_CONFIG_ID,
                    validator.msg("error_no_id", "The system configuration has no id"));
        }
    }

    private void checkUser(UserEntity loggedInUser) {
        if (!UserUtils.hasRole(loggedInUser.getRole(), UserRole.ADMIN)) {
            throw new AuthorizationException(ErrorCode.USER_NOT_AUTHORIZED, validator.msg("error_not_authorized"));
        }

        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(ErrorCode.USER_NOT_AUTHORIZED, validator.msg("error_not_authorized"));
        }
    }

    private void checkOpeningAndClosingHour(ReservationSystemConfig config) {
        validator.checkInt("openingHour", config.openingHour(), 0, 24);
        validator.checkInt("closingHour", config.closingHour(), 0, 24);
        if (config.openingHour() >= config.closingHour()) {
            validator.addFieldErrorMessage("openingHour", ErrorCode.OPENING_HOUR_AFTER_CLOSING_HOUR, "error_opening_hour_after_closing_hour");
        }
    }

    private void checkTypes(ReservationSystemConfig config) {
        if (ObjectUtils.isEmpty(config.types())) {
            validator.addFieldErrorMessage("reservationTypes", ErrorCode.NO_RESERVATION_TYPES, "error_no_reservation_types");
        } else {
            config.types().forEach(this::checkType);
        }
    }

    private void checkType(SystemConfigReservationType reservationType) {
        validator.checkInt("reservationtype.type", reservationType.type(), 0, 20);
        checkString("reservationTypes", reservationType.name());
    }

    private void checkString(String field, String value) {
        validator.checkString(field, value, MIN_STRING_LENGTH, MAX_STRING_LENGTH);
    }
}
