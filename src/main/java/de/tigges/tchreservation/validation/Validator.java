package de.tigges.tchreservation.validation;

import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.exception.ErrorMessage;
import de.tigges.tchreservation.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Validator {
    public final MessageSource messageSource;
    private final Collection<ErrorMessage> errorMessages = new LinkedList<>();

    public void clearErrorMessages() {
        errorMessages.clear();
    }

    public void checkErrorMessages() throws InvalidDataException {
        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
        }
    }

    public void addErrorMessages(Collection<ErrorMessage> errorMessages) {
        this.errorMessages.addAll(errorMessages);
    }

    public void addErrorMessage(ErrorCode code, String message, Object... args) {
        errorMessages.add(new ErrorMessage(code, msg(message, args), null));
    }

    public void addFieldErrorMessage(String field, ErrorCode code, String message, Object... args) {
        errorMessages.add(new ErrorMessage(code, msg(message, args), field));
    }

    public String msg(String code, Object... args) {
        var message = messageSource.getMessage(code, null, Locale.getDefault());
        return args.length > 0 ? message.formatted(args) : message;
    }

    public void checkString(String field, String value, int minLen, int maxLen) {
        if (checkNotEmpty(field, value)) {
            if (value.length() < minLen) {
                addFieldErrorMessage(field, ErrorCode.STRING_TOO_SHORT, "error_string_too_short", minLen);
            } else if (value.length() > maxLen) {
                addFieldErrorMessage(field, ErrorCode.STRING_TOO_LONG, "error_string_too_long", maxLen);
            }
        }
    }

    public boolean checkNotEmpty(String field, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            addFieldErrorMessage(field, ErrorCode.NULL_NOT_ALLOWED, "error_null_not_allowed");
            return false;
        }
        return true;
    }

    public void checkInt(String field, int value, int minValue, int maxValue) {
        if (value < minValue) {
            addFieldErrorMessage(field, ErrorCode.NUMBER_TOO_SMALL, "error_value_too_small", minValue);
        }
        if (value > maxValue) {
            addFieldErrorMessage(field, ErrorCode.NUMBER_TOO_BIG, "error_value_too_big", maxValue);
        }
    }
}
