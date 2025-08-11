package de.tigges.tchreservation.util.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            jsonGenerator.writeString(Long.toString(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        }
    }
}
