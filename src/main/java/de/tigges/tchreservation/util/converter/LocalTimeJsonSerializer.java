package de.tigges.tchreservation.util.converter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class LocalTimeJsonSerializer extends ValueSerializer<LocalTime> {

	@Override
	public void serialize(LocalTime value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
		if (value != null) {
			gen.writeString(Long.toString(value
							.atDate(LocalDate.ofEpochDay(0))
							.atZone(ZoneId.systemDefault())
							.toInstant()
							.toEpochMilli()));
		}
	}
}
