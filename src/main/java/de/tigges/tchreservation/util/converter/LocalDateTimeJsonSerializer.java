package de.tigges.tchreservation.util.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeJsonSerializer extends ValueSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator jsonGenerator, SerializationContext serializerProvider) throws JacksonException {
        if (value != null) {
            jsonGenerator.writeString(Long.toString(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        }
    }
}
