package de.tigges.tchreservation.util.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalDateJsonDeserializer extends ValueDeserializer<LocalDate> {

	@Override
	public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {

		if (jsonParser.currentToken().isNumeric()) {
			return toLocalDate(jsonParser.getLongValue());
		}

		var stringValue = jsonParser.getString();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalDate(Long.parseLong(stringValue));
		}

		log.error("cannot deserialize local date {}", jsonParser.currentToken());
		throw new IllegalArgumentException("cannot deserialize token to Local Date " + jsonParser.currentToken());
	}

	private LocalDate toLocalDate(long value) {
		return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
