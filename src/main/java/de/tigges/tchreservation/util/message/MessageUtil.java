package de.tigges.tchreservation.util.message;

import de.tigges.tchreservation.util.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageUtil {
    private final MessageSource messageSource;

    public String msg(ErrorCode code, Object... args) {
        var message = messageSource.getMessage("error_" + code.name().toLowerCase(), null, Locale.getDefault());
        return args.length > 0 ? message.formatted(args) : message;
    }
}
