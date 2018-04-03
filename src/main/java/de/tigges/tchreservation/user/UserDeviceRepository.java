package de.tigges.tchreservation.user;

import org.springframework.data.repository.CrudRepository;

import de.tigges.tchreservation.user.model.UserDevice;

public interface UserDeviceRepository extends CrudRepository<UserDevice,Long> {
//    List<UserDevice> findByUserId(long id);
}
