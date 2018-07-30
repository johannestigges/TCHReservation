package de.tigges.tchreservation.converter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalTimeJsonDeserializer extends JsonDeserializer<LocalTime> {
	public static final Logger logger = LoggerFactory.getLogger(LocalTimeJsonDeserializer.class);


	@Override
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		if (p.currentToken().isNumeric()) {
			return toLocalTime(p.getLongValue());
		}
		
		String stringValue = p.getText();
		if (stringValue != null && !stringValue.isEmpty()) {
			return toLocalTime(Long.valueOf(stringValue));
		}
		
		logger.error("cannot deserialize local time " + p.getCurrentToken());
		throw new IllegalArgumentException("cannot deserialize token to local time");
	}
	
	private LocalTime toLocalTime(long timeValue) {
		return Instant.ofEpochMilli(timeValue).atZone(ZoneId.systemDefault()).toLocalTime();
	}
}
