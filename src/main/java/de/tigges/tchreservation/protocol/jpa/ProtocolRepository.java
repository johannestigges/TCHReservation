package de.tigges.tchreservation.protocol.jpa;

import java.time.LocalDateTime;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.protocol.EntityType;

public interface ProtocolRepository extends CrudRepository<ProtocolEntity, Long> {

	Iterable<ProtocolEntity> findByEntityTypeAndEntityId(EntityType entityType, Long id);

	Iterable<ProtocolEntity> findByTimeGreaterThan(LocalDateTime time);
}
