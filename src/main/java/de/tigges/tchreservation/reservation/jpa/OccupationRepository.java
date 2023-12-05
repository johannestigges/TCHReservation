package de.tigges.tchreservation.reservation.jpa;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface OccupationRepository extends CrudRepository<OccupationEntity, Long> {

    Iterable<OccupationEntity> findBySystemConfigIdAndDate(long systemConfigId, LocalDate date);

    Iterable<OccupationEntity> findByReservationId(long reservationId);
}
