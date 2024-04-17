package de.tigges.tchreservation.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
public class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.currentToken().isNumeric()) {
            return toLocalDateTime(jsonParser.getLongValue());
        }

        var stringValue = jsonParser.getText();
        if (stringValue != null && !stringValue.isEmpty()) {
            return toLocalDateTime(Long.parseLong(stringValue));
        }

        log.error("cannot deserialize local datetime {}", jsonParser.getCurrentToken());
        throw new IllegalArgumentException("cannot deserialize token to LocalDateTime " + jsonParser.getCurrentToken());
    }

    private LocalDateTime toLocalDateTime(long value) {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
