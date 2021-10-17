package de.tigges.tchreservation.converter;

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
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		if (p.currentToken().isNumeric()) {
			return toLocalDate(p.getLongValue());
		}

		String stringValue = p.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalDate(Long.valueOf(stringValue));
		}

		log.error("cannot deserialize local date {}", p.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to Local Date " + p.getCurrentToken());
	}

	private LocalDate toLocalDate(long value) {
		return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();

	}
}
