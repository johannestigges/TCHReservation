package de.tigges.tchreservation.user;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.user.jpa.UserDeviceEntity;

public interface UserDeviceRepository extends CrudRepository<UserDeviceEntity, Long> {
}
