package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.model.UserDevice;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDeviceRepository extends CrudRepository<UserDevice,Long> {
    List<UserDevice> findByUserUserId(long userId);
}
