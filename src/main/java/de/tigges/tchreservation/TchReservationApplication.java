package de.tigges.tchreservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.tigges.tchreservation.converter.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class TchReservationApplication
{
	/**
	 * register date converters
	 * <p>
	 * all date and time data is transferred in json as unix epoch timestamps
	 * (milliseconds since 1970-01-01)
	 * <p>
	 * return {@link Jackson2ObjectMapperBuilder}
	 */
	@Bean
	public Jackson2ObjectMapperBuilder objectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializerByType(LocalDate.class, new LocalDateJsonSerializer());
		builder.deserializerByType(LocalDate.class, new LocalDateJsonDeserializer());
		builder.serializerByType(LocalTime.class, new LocalTimeJsonSerializer());
		builder.deserializerByType(LocalTime.class, new LocalTimeJsonDeserializer());
		builder.serializerByType(LocalDateTime.class, new LocalDateTimeJsonSerializer());
		builder.deserializerByType(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
		return builder;
	}

	public static void main(String[] args) {
		SpringApplication.run(TchReservationApplication.class, args);
	}
}
