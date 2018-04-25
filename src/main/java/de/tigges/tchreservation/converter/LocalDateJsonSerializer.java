package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateJsonSerializer extends JsonSerializer<LocalDate> {

	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(Long.toString(value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
	}
}
