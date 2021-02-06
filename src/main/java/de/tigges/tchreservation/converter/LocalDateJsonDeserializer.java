package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {

	public static final Logger logger = LoggerFactory.getLogger(LocalDateJsonDeserializer.class);

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		if (p.currentToken().isNumeric()) {
			return toLocalDate(p.getLongValue());
		}

		String stringValue = p.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalDate(Long.valueOf(stringValue));
		}

		logger.error("cannot deserialize local date " + p.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to Local Date " + p.getCurrentToken());
	}

	private LocalDate toLocalDate(long value) {
		return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();

	}
}
