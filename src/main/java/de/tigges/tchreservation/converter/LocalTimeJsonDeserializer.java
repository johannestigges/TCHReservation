package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalTimeJsonDeserializer extends JsonDeserializer<LocalTime> {

	@Override
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (p.currentToken().isNumeric()) {
			return Instant.ofEpochMilli(p.getLongValue()).atZone(ZoneId.systemDefault()).toLocalTime();
		}

		if (p.currentToken().asByteArray() == null || p.currentToken().asByteArray().length == 0) {
			return null;
		}
		
		throw new IllegalArgumentException("cannot convert token to Local Time");
	}
}
