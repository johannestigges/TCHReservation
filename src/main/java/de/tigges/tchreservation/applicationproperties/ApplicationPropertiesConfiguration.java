package de.tigges.tchreservation.applicationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "reservation")
@Data
public class ApplicationPropertiesConfiguration {

	private String title;
}
