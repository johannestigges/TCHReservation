package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserDeviceEntity;
import de.tigges.tchreservation.user.model.UserDevice;

public class UserDeviceMapper {

	private UserDeviceMapper() {
		// avoid instantiation
	}

	public static UserDevice map(UserDeviceEntity userDeviceEntity) {
		if (userDeviceEntity == null) {
			return null;
		}
		var userDevice = new UserDevice();
		userDevice.setId(userDeviceEntity.getId());
		userDevice.setUser(UserMapper.map(userDeviceEntity.getUser()));
		userDevice.setDeviceId(userDeviceEntity.getDeviceId());
		userDevice.setStatus(userDeviceEntity.getStatus());
		userDevice.setPublicKey(userDeviceEntity.getPublicKey());
		return userDevice;
	}

	public static UserDeviceEntity map(UserDevice userDevice) {
		if (userDevice == null) {
			return null;
		}
		var userDeviceEntity = new UserDeviceEntity();
		userDeviceEntity.setId(userDevice.getId());
		userDeviceEntity.setUser(UserMapper.map(userDevice.getUser()));
		userDeviceEntity.setDeviceId(userDevice.getDeviceId());
		userDeviceEntity.setStatus(userDevice.getStatus());
		userDeviceEntity.setPublicKey(userDevice.getPublicKey());
		return userDeviceEntity;
	}
}
