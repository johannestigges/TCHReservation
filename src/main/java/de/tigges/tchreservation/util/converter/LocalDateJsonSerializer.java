package de.tigges.tchreservation.util.converter;

import java.time.LocalDate;
import java.time.ZoneId;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class LocalDateJsonSerializer extends ValueSerializer<LocalDate> {

	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
		if (value != null) {
			gen.writeString(Long.toString(value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
		}
	}
}
