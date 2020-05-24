package de.tigges.tchreservation.reservation;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Configuration
@ConfigurationProperties(prefix = "reservation")
@Data
@Log4j2
public class ReservationSystemConfiguration {

	private List<ReservationSystemConfig> config;

	public void setConfig(List<ReservationSystemConfig> config) {
		this.config = config;
		if (log.isInfoEnabled()) {
			log.info("set ReservationSystemConfig " + config);
		}
	}
}
