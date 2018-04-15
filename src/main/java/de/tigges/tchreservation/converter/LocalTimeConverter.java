package de.tigges.tchreservation.converter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;

public class LocalTimeConverter implements Converter<String, LocalTime>{

	@Override
	public LocalTime convert(String source) {
		return Instant.ofEpochMilli(
				Long.parseLong(source))
				.atZone(ZoneId.systemDefault())
				.toLocalTime();
	}
}
