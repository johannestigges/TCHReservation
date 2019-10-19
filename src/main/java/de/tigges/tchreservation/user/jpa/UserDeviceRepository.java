package de.tigges.tchreservation.user.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserDeviceRepository extends CrudRepository<UserDeviceEntity, Long> {

	List<UserDeviceEntity> findByUserId(long userId);
}
