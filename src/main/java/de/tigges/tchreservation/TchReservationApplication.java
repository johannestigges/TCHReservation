package de.tigges.tchreservation;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.tigges.tchreservation.converter.LocalDateJsonDeserializer;
import de.tigges.tchreservation.converter.LocalDateJsonSerializer;
import de.tigges.tchreservation.converter.LocalTimeJsonDeserializer;
import de.tigges.tchreservation.converter.LocalTimeJsonSerializer;

@SpringBootApplication
public class TchReservationApplication
{
	/**
	 * register date converters
	 * 
	 * all date and time data is transferred in json as unix epoch timestamps
	 * (milliseconds since 1970-01-01)
	 *
	 * return {@link Jackson2ObjectMapperBuilder}
	 */
	@Bean
	public Jackson2ObjectMapperBuilder objectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializerByType(LocalDate.class, new LocalDateJsonSerializer());
		builder.deserializerByType(LocalDate.class, new LocalDateJsonDeserializer());
		builder.serializerByType(LocalTime.class, new LocalTimeJsonSerializer());
		builder.deserializerByType(LocalTime.class, new LocalTimeJsonDeserializer());
		return builder;
	}

	public static void main(String[] args) {
		SpringApplication.run(TchReservationApplication.class, args);
	}
}