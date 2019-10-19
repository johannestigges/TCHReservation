package de.tigges.tchreservation.reservation.jpa;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<ReservationEntity, Long> {

	Iterable<ReservationEntity> findByUserId(long userId);

	Iterable<ReservationEntity> findBySystemConfigIdAndDate(int systemConfigId, LocalDate date);
}
