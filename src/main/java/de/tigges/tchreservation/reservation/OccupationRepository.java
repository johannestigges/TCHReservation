package de.tigges.tchreservation.reservation;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.reservation.model.Occupation;

public interface OccupationRepository extends CrudRepository<Occupation, Long> {
	
	public Iterable<Occupation> findBySystemConfigIdAndDate(long systemConfigId, LocalDate date);
	
	public Iterable<Occupation> findByReservationId(long reservationId);

}
