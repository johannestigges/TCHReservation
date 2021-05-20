package de.tigges.tchreservation.reservation;

import org.springframework.stereotype.Component;

import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationSystemConfigRepository {

	private final ReservationSystemConfiguration properties;

	public ReservationSystemConfig get(long id) {
		// @formatter:off
 		return properties.getConfig()
 				.stream()
 				.filter(s -> id == s.getId())
 				.findFirst()
				.orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id));
		// @formatter:on
	}
}
