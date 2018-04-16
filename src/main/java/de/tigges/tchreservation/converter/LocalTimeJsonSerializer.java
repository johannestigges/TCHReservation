package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalTimeJsonSerializer extends JsonSerializer<LocalTime> {

	@Override
	public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(
				Long.toString(
						value.atDate(LocalDate.now())
						.atZone(ZoneId.systemDefault())
						.toEpochSecond()));
	}

}
