package de.tigges.tchreservation.systemconfig.jpa;

import org.springframework.data.repository.CrudRepository;

public interface ReservationTypeRepository extends CrudRepository<ReservationTypeEntity, Long> {

    Iterable<ReservationTypeEntity> findBySystemConfigId(long systemConfigId);
    void deleteBySystemConfigId(long systemConfigId);
}
