package de.tigges.tchreservation;

import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.validation.Validator;
import org.junit.jupiter.api.function.Executable;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidatorTest {
    public MessageSource messageSourceMock = mock(MessageSource.class);


    public void checkError(Executable executable, String expectedErrorCode) {
        initMessageSource(expectedErrorCode, "Pfui!");
        var exception = assertThrows(BadRequestException.class, executable);
        assertTrue(exception.getErrorMessages().stream()
                .anyMatch(e -> "Pfui!".equals(e.message())));
    }

    public void checkFieldError(Executable executable, String expectedErrorCode, String expectedField) {
        initMessageSource(expectedErrorCode, "Pfui!");
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertTrue(exception.getErrorMessages()
                .stream().anyMatch(e -> "Pfui!".equals(e.message()) && expectedField.equals(e.field())));
    }

    public void initMessageSource(String msg, String value) {
        when(messageSourceMock.getMessage(eq(msg), any(), eq(Locale.getDefault()))).thenReturn(value);
    }

    public Validator createValidator() {
        return new Validator(messageSourceMock);
    }
}
