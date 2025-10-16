package de.tigges.tchreservation.util.message;

import de.tigges.tchreservation.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageUtil {
    private final MessageSource messageSource;

    public String msg(ErrorCode code, Object... args) {
        return messageSource.getMessage("error_" + code.name().toLowerCase(), args, Locale.getDefault());
    }
}
