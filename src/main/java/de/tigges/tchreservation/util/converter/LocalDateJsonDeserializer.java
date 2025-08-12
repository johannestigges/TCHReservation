package de.tigges.tchreservation.util.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {

	@Override
	public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

		if (jsonParser.currentToken().isNumeric()) {
			return toLocalDate(jsonParser.getLongValue());
		}

		var stringValue = jsonParser.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalDate(Long.parseLong(stringValue));
		}

		log.error("cannot deserialize local date {}", jsonParser.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to Local Date " + jsonParser.getCurrentToken());
	}

	private LocalDate toLocalDate(long value) {
		return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
