package de.tigges.tchreservation.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;

public class LocalDateConverter implements Converter<String, LocalDate> {

	@Override
	public LocalDate convert(String source) {
		return Instant.ofEpochMilli(Long.parseLong(source))
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}
}
