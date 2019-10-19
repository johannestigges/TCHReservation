package de.tigges.tchreservation.reservation.jpa;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

public interface OccupationRepository extends CrudRepository<OccupationEntity, Long> {

	public Iterable<OccupationEntity> findBySystemConfigIdAndDate(long systemConfigId, LocalDate date);

	public Iterable<OccupationEntity> findByReservationId(long reservationId);

	public Iterable<OccupationEntity> findBySystemConfigId(long systemConfigId);

	public void deleteByReservationId(long id);
}
