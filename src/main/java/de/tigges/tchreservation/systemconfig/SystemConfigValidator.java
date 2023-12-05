package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.ErrorMessage;
import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SystemConfigValidator {

    private static final int MAX_COURTS = 20;
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    private final MessageSource messageSource;

    public void validate(ReservationSystemConfig config, UserEntity loggedInUser) {
        var errorMessages = new ArrayList<ErrorMessage>();

        if (config.id() < 1) {
            throw new BadRequestException(msg("error_no_id"));
        }

        if (!UserUtils.hasRole(loggedInUser.getRole(), UserRole.ADMIN)) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        checkString(config.name(), errorMessages, "name");

        if (config.courts() == null || config.courts().isEmpty()) {
            addFieldError(errorMessages, "courts", msg("error_null_not_allowed"));
        } else {
            if (config.courts().size() > MAX_COURTS) {
                addFieldError(errorMessages, "courts", msg("error_too_many_courts"));
            }
            config.courts().forEach(court ->
                    checkString(court, errorMessages, "court"));
        }
        checkInt(config.durationUnitInMinutes(), errorMessages, "durationUnitInMinutes", 30, 60);
        checkInt(config.maxDaysReservationInFuture(), errorMessages, "maxDaysReservationInFuture", 1, 365);
        checkInt(config.maxDuration(), errorMessages, "maxDuration", 1, 20);

        checkInt(config.openingHour(), errorMessages, "openingHour", 0, 24);
        checkInt(config.closingHour(), errorMessages, "closingHour", 0, 24);
        if (config.openingHour() >= config.closingHour()) {
            addFieldError(errorMessages, "openingHour", msg("error_opening_hour_after_closing_hour"));
        }

        if (config.types() == null || config.types().isEmpty()) {
            addFieldError(errorMessages, "reservationTypes", msg("error_no_reservation_types"));
        } else {
            config.types().forEach(t -> checkType(t, errorMessages));
        }

        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
        }
    }

    private void checkType(SystemConfigReservationType reservationType, Collection<ErrorMessage> errorMessages) {
        checkInt(reservationType.type(), errorMessages, "reservationtype.type", 0, 20);
        checkString(reservationType.name(), errorMessages, "reservationTypes");
    }

    private void checkString(String value, Collection<ErrorMessage> errorMessages, String field) {
        if (value == null || value.isEmpty()) {
            addFieldError(errorMessages, field, msg("error_null_not_allowed"));
        } else if (value.length() < SystemConfigValidator.MIN_LENGTH) {
            addFieldError(errorMessages, field, String.format(msg("error_string_too_short"), SystemConfigValidator.MIN_LENGTH));
        } else if (value.length() > SystemConfigValidator.MAX_LENGTH) {
            addFieldError(errorMessages, field, String.format(msg("error_string_too_long"), SystemConfigValidator.MAX_LENGTH));
        }
    }

    private void checkInt(int value, Collection<ErrorMessage> errorMessages, String field, int minValue, int maxValue) {
        if (value < minValue) {
            addFieldError(errorMessages, field, msg("error_value_too_small"));
        }
        if (value > maxValue) {
            addFieldError(errorMessages, field, msg("error_value_too_big"));
        }
    }

    private void addFieldError(Collection<ErrorMessage> errorMessages, String field, String message) {
        errorMessages.add(new ErrorMessage(message, null, field));
    }

    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }
}
