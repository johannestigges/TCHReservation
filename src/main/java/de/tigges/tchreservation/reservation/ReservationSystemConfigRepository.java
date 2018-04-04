package de.tigges.tchreservation.reservation;

import org.springframework.stereotype.Component;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

@Component
public class ReservationSystemConfigRepository {

	private static final ReservationSystemConfig system1 = new ReservationSystemConfig(1, "Außenplätze", 6, 30, 8, 22);
	private static final ReservationSystemConfig system2 = new ReservationSystemConfig(2, "Hallenplätze", 2, 60, 8, 22);

	public ReservationSystemConfig get(long id) {
		switch ((int) id) {
		case 1:
			return system1;
		case 2:
			return system2;
		default:
			throw new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id);
		}
	}
}
