package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.exception.*;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SystemConfigValidator {

    private static final int MAX_COURTS = 20;

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    private final MessageSource messageSource;

    public void validate(ReservationSystemConfig config, UserEntity loggedInUser) {
        ErrorDetails errorDetails = new ErrorDetails(msg("error_validation_config"), null);

        if (config.getId() < 1) {
            throw new BadRequestException(msg("error_no_id"));
        }

        if (!UserUtils.hasRole(loggedInUser.getRole(), UserRole.ADMIN)) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        checkString(config.getName(), errorDetails, "name");

        if (config.getCourts() == null || config.getCourts().isEmpty()) {
            addFieldError(errorDetails, "courts", msg("error_null_not_allowed"));
        }
        if (config.getCourts().size() > MAX_COURTS) {
            addFieldError(errorDetails, "courts", msg("error_too_many_courts"));
        }
        config.getCourts().forEach(court -> checkString(court, errorDetails, "court"));

        checkInt(config.getDurationUnitInMinutes(), errorDetails, "durationUnitInMinutes", 30, 60);
        checkInt(config.getMaxDaysReservationInFuture(), errorDetails, "maxDaysReservationInFuture", 1, 365);
        checkInt(config.getMaxDuration(), errorDetails, "maxDuration", 1, 20);

        checkInt(config.getOpeningHour(), errorDetails, "openingHour", 0, 24);
        checkInt(config.getClosingHour(), errorDetails, "closingHour", 0, 24);
        if (config.getOpeningHour() >= config.getClosingHour()) {
            addFieldError(errorDetails, "openingHour", msg("error_opening_hour_after_closing_hour"));
        }

        /* not working yet
        if (config.getTypes() == null || config.getTypes().isEmpty()) {
            addFieldError(errorDetails, "reservationTypes", msg("error_no_reservation_types"));
        }
        config.getTypes().forEach(t -> checkType(t, errorDetails));
        */
        if (!errorDetails.getFieldErrors().isEmpty()) {
            throw new InvalidDataException(errorDetails);
        }
    }

    private void checkType(SystemConfigReservationType reservationType, ErrorDetails errorDetails) {
        checkInt(reservationType.getType(), errorDetails, "reservationtype.type", 1, 20);
        checkString(reservationType.getName(), errorDetails, "reservationTypes");
    }

    private void checkString(String value, ErrorDetails errorDetails, String field) {
        if (value == null || value.length() < MIN_LENGTH) {
            addFieldError(errorDetails, field, msg("error_null_not_allowed"));
        } else if (value.length() > MAX_LENGTH) {
            addFieldError(errorDetails, field, msg("error_string_too_long"));
        }
    }

    private void checkInt(int value, ErrorDetails errorDetails, String field, int minValue, int maxValue) {
        if (value < minValue) {
            addFieldError(errorDetails, field, msg("error_value_too_small"));
        }
        if (value > maxValue) {
            addFieldError(errorDetails, field, msg("error_value_too_big"));
        }
    }

    private void addFieldError(ErrorDetails errorDetails, String field, String message) {
        errorDetails.getFieldErrors().add(new FieldError("systemConfig", field, message));
    }

    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }
}
