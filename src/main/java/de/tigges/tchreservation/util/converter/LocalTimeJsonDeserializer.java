package de.tigges.tchreservation.util.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalTimeJsonDeserializer extends JsonDeserializer<LocalTime> {

	@Override
	public LocalTime deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException {

		if (jsonParser.currentToken().isNumeric()) {
			return toLocalTime(jsonParser.getLongValue());
		}

		var stringValue = jsonParser.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalTime(Long.parseLong(stringValue));
		}

		log.error("cannot deserialize local time {}", jsonParser.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to local time " + jsonParser.getCurrentToken());
	}

	private LocalTime toLocalTime(long timeValue) {
		return Instant.ofEpochMilli(timeValue).atZone(ZoneId.systemDefault()).toLocalTime();
	}
}
