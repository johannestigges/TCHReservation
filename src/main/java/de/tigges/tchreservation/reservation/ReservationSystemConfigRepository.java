package de.tigges.tchreservation.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

@Component
public class ReservationSystemConfigRepository {

	@Autowired
	ReservationSystemConfiguration properties;

	public ReservationSystemConfig get(long id) {
		return properties.getConfig().stream().filter(s -> id == s.getId()).findFirst()
				.orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id));
	}
}
