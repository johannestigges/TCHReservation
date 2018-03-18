package de.tigges.tchreservation.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class UserDevice {

	public UserDevice() {
	}

	public UserDevice(User user, String deviceId, ActivationStatus status, String publicKey) {
		this.user = user;
		this.deviceId = deviceId;
		this.status = status;
		this.publicKey = publicKey;
	}

	@Id
	@GeneratedValue
	private long id;

	@JsonIgnore
	@ManyToOne
	private User user;

	private String deviceId; // unique device id, such as ISME or MAC address
	private ActivationStatus status;
	private String publicKey;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public void setAtstus(ActivationStatus status) {
		this.status = status;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
