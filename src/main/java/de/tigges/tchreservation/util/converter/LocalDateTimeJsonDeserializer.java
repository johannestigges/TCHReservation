package de.tigges.tchreservation.util.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
public class LocalDateTimeJsonDeserializer extends ValueDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        if (jsonParser.currentToken().isNumeric()) {
            return toLocalDateTime(jsonParser.getLongValue());
        }

        var stringValue = jsonParser.getString();
        if (stringValue != null && !stringValue.isEmpty()) {
            return toLocalDateTime(Long.parseLong(stringValue));
        }

        log.error("cannot deserialize local datetime {}", jsonParser.currentToken());
        throw new IllegalArgumentException("cannot deserialize token to LocalDateTime " + jsonParser.currentToken());
    }

    private LocalDateTime toLocalDateTime(long value) {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
