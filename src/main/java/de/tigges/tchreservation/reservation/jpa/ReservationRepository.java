package de.tigges.tchreservation.reservation.jpa;

import de.tigges.tchreservation.user.jpa.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<ReservationEntity, Long> {
    Iterable<ReservationEntity> findByUserOrderByDateDesc(UserEntity user);
}
