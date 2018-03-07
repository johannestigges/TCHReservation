package de.tigges.tchreservation.reservation.model;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface OccupationRepository extends CrudRepository<OccupationEntity, Long> {
    List<OccupationEntity> findByStartGreaterThanEqual(LocalDate start);
}
