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

        if (config.getId() < 1) {
            throw new BadRequestException(msg("error_no_id"));
        }

        if (!UserUtils.hasRole(loggedInUser.getRole(), UserRole.ADMIN)) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        if (!ActivationStatus.ACTIVE.equals(loggedInUser.getStatus())) {
            throw new AuthorizationException(msg("error_not_authorized"));
        }

        checkString(config.getName(), errorMessages, "name", MIN_LENGTH, MAX_LENGTH);

        if (config.getCourts() == null || config.getCourts().isEmpty()) {
            addFieldError(errorMessages, "courts", msg("error_null_not_allowed"));
        }
        if (config.getCourts().size() > MAX_COURTS) {
            addFieldError(errorMessages, "courts", msg("error_too_many_courts"));
        }
        config.getCourts().forEach(court ->
                checkString(court, errorMessages, "court", MIN_LENGTH, MAX_LENGTH));

        checkInt(config.getDurationUnitInMinutes(), errorMessages, "durationUnitInMinutes", 30, 60);
        checkInt(config.getMaxDaysReservationInFuture(), errorMessages, "maxDaysReservationInFuture", 1, 365);
        checkInt(config.getMaxDuration(), errorMessages, "maxDuration", 1, 20);

        checkInt(config.getOpeningHour(), errorMessages, "openingHour", 0, 24);
        checkInt(config.getClosingHour(), errorMessages, "closingHour", 0, 24);
        if (config.getOpeningHour() >= config.getClosingHour()) {
            addFieldError(errorMessages, "openingHour", msg("error_opening_hour_after_closing_hour"));
        }

        if (config.getTypes() == null || config.getTypes().isEmpty()) {
            addFieldError(errorMessages, "reservationTypes", msg("error_no_reservation_types"));
        }
        config.getTypes().forEach(t -> checkType(t, errorMessages));

        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
        }
    }

    private void checkType(SystemConfigReservationType reservationType, Collection<ErrorMessage> errorMessages) {
        checkInt(reservationType.getType(), errorMessages, "reservationtype.type", 0, 20);
        checkString(reservationType.getName(), errorMessages, "reservationTypes", MIN_LENGTH, MAX_LENGTH);
    }

    private void checkString(String value, Collection<ErrorMessage> errorMessages, String field, int minLen, int maxLen) {
        if (value == null || value.isEmpty()) {
            addFieldError(errorMessages, field, msg("error_null_not_allowed"));
        } else if (value.length() < minLen) {
            addFieldError(errorMessages, field, String.format(msg("error_string_too_short"),minLen));
        } else if (value.length() > maxLen) {
            addFieldError(errorMessages, field, String.format(msg("error_string_too_long"),maxLen));
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
        errorMessages.add(new ErrorMessage(message,null, field));
    }

    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }
}
