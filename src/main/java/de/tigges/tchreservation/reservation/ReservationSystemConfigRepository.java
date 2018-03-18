package de.tigges.tchreservation.reservation;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;

public interface ReservationSystemConfigRepository extends CrudRepository<ReservationSystemConfig, Long> {

}
