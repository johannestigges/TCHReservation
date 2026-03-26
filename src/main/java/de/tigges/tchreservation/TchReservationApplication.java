package de.tigges.tchreservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.tigges.tchreservation.util.converter.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;

@SpringBootApplication
public class TchReservationApplication
{
	/**
	 * register date converters
	 * <p>
	 * all date and time data is transferred in json as unix epoch timestamps
	 * (milliseconds since 1970-01-01)
	 */
	@Bean
	public SimpleModule dateConvertersModule() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
		module.addDeserializer(LocalDate.class, new LocalDateJsonDeserializer());
		module.addSerializer(LocalTime.class, new LocalTimeJsonSerializer());
		module.addDeserializer(LocalTime.class, new LocalTimeJsonDeserializer());
		module.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
		module.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
		return module;
	}

	public static void main(String[] args) {
		SpringApplication.run(TchReservationApplication.class, args);
	}
}
