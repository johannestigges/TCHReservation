package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		
		if (p.currentToken().isNumeric()) {
			return Instant.ofEpochMilli(p.getLongValue()).atZone(ZoneId.systemDefault()).toLocalDate();
		}

		if (p.currentToken().asByteArray() == null || p.currentToken().asByteArray().length == 0) {
			return null;
		}
		
		throw new IllegalArgumentException("cannot convert token to Local Date");
	}
}
