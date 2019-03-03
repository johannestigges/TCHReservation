package de.tigges.tchreservation.user.model;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;

@Entity
public class UserDevice implements Protocollable {

	public UserDevice() {
	}

	public UserDevice(User user, String deviceId, ActivationStatus status, String publicKey) {
		setUser(user);
		setDeviceId(deviceId);
		setStatus(status);
		setPublicKey(publicKey);
	}

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(optional = false)
	private User user;

	@Column(nullable = true)
	private String deviceId; // unique device id, such as ISME or MAC address

	@Column(nullable = false)
	private ActivationStatus status;

	@Column(nullable = true)
	private String publicKey;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getUser() {
		// make a copy to avoid infinite loop with user.getDevices()!!
		return new User(user);
	}

	public void setUser(User user) {
		// make a copy of user to avoid infinite loop with user.getDevices()!!
		this.user = new User(user);
		this.user.getDevices().clear();
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ActivationStatus getStatus() {
		return status;
	}

	public void setStatus(ActivationStatus status) {
		this.status = status;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields(//
				"user", user.getName(), //
				"deviceId", deviceId, //
				"publicKey", publicKey, //
				"status", status.name());
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.USER_DEVICE;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}
}
