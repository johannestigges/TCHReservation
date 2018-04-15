package de.tigges.tchreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.tigges.tchreservation.converter.LocalDateConverter;
import de.tigges.tchreservation.converter.LocalTimeConverter;

@SpringBootApplication
public class TchReservationApplication implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new LocalDateConverter());
		registry.addConverter(new LocalTimeConverter());
	}
	
    public static void main(String[] args) {
        SpringApplication.run(TchReservationApplication.class, args);
    }
}
