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
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException {

		if (p.currentToken().isNumeric()) {
			return toLocalTime(p.getLongValue());
		}

		var stringValue = p.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalTime(Long.parseLong(stringValue));
		}

		log.error("cannot deserialize local time {}", p.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to local time " + p.getCurrentToken());
	}

	private LocalTime toLocalTime(long timeValue) {
		return Instant.ofEpochMilli(timeValue).atZone(ZoneId.systemDefault()).toLocalTime();
	}
}
