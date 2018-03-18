package de.tigges.tchreservation.reservation;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.reservation.model.Occupation;

public interface OccupationRepository extends CrudRepository<Occupation, Long> {

}
