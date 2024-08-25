package de.tigges.tchreservation;

import de.tigges.tchreservation.exception.ErrorMessage;
import de.tigges.tchreservation.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Validator {
    public final MessageSource messageSource;
    Collection<ErrorMessage> errorMessages = new HashSet<>();

    public void startValidation() {
        errorMessages.clear();
    }

    public void checkErrorMessages() throws InvalidDataException {
        if (!errorMessages.isEmpty()) {
            throw new InvalidDataException(errorMessages);
        }
    }
    public void addErrorMessage(String code, Object...args) {
        errorMessages.add(new ErrorMessage(msg(code, args), null, null));
    }
    public void addFieldErrorMessage(String field, String code, Object...args) {
        errorMessages.add(new ErrorMessage(msg(code, args), null, field));
    }
    public String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.getDefault());
    }
}
