package de.tigges.tchreservation.reservation;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.reservation.model.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	Iterable<Reservation> findByUserId(long userId);
}
