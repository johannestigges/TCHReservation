package de.tigges.tchreservation.reservation;

import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	Iterable<Reservation> findByUserId(long userId);
}
