package de.tigges.tchreservation.protocol;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.EntityType;

public interface ProtocolRepository extends CrudRepository<Protocol, Long> {

	Iterable<Protocol> findByEntityTypeAndEntityId(EntityType entityType, Long id);
}
