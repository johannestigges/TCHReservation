package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserDeviceEntity;
import de.tigges.tchreservation.user.model.UserDevice;

public class UserDeviceMapper {

	private UserDeviceMapper() {
		// avoid instantiation
	}

	public static UserDevice map(UserDeviceEntity e) {
		if (e == null) {
			return null;
		}
		UserDevice u = new UserDevice();
		u.setId(e.getId());
		u.setUser(UserMapper.map(e.getUser()));
		u.setDeviceId(e.getDeviceId());
		u.setStatus(e.getStatus());
		u.setPublicKey(e.getPublicKey());
		return u;

	}

	public static UserDeviceEntity map(UserDevice u) {
		if (u == null) {
			return null;
		}
		UserDeviceEntity e = new UserDeviceEntity();
		e.setId(u.getId());
		e.setUser(UserMapper.map(u.getUser()));
		e.setDeviceId(u.getDeviceId());
		e.setStatus(u.getStatus());
		e.setPublicKey(u.getPublicKey());
		return e;
	}
}
