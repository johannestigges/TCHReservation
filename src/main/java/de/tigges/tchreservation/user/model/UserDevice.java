package de.tigges.tchreservation.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDevice {

	public UserDevice(User user, String deviceId, ActivationStatus status, String publicKey) {
		setUser(user);
		setDeviceId(deviceId);
		setStatus(status);
		setPublicKey(publicKey);
	}

	private long id;

	private User user;

	private String deviceId; // unique device id, such as ISME or MAC address

	private ActivationStatus status;

	private String publicKey;

	public User getUser() {
		// make a copy to avoid infinite loop with user.getDevices()!!
		return new User(user);
	}

	public void setUser(User user) {
		// make a copy of user to avoid infinite loop with user.getDevices()!!
		this.user = new User(user);
		this.user.getDevices().clear();
	}
}
