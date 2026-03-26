package de.tigges.tchreservation.util.converter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalTimeJsonDeserializer extends ValueDeserializer<LocalTime> {

	@Override
	public LocalTime deserialize(JsonParser jsonParser, DeserializationContext context)
			throws JacksonException {

		if (jsonParser.currentToken().isNumeric()) {
			return toLocalTime(jsonParser.getLongValue());
		}

		var stringValue = jsonParser.getString();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalTime(Long.parseLong(stringValue));
		}

		log.error("cannot deserialize local time {}", jsonParser.currentToken());
		throw new IllegalArgumentException("cannot deserialize token to local time " + jsonParser.currentToken());
	}

	private LocalTime toLocalTime(long timeValue) {
		return Instant.ofEpochMilli(timeValue).atZone(ZoneId.systemDefault()).toLocalTime();
	}
}
