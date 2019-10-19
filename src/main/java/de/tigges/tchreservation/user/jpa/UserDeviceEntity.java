package de.tigges.tchreservation.user.jpa;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.user.model.ActivationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_device")
@Data
@NoArgsConstructor
public class UserDeviceEntity implements Protocollable {

	public UserDeviceEntity(UserEntity user, String deviceId, ActivationStatus status, String publicKey) {
		setUser(user);
		setDeviceId(deviceId);
		setStatus(status);
		setPublicKey(publicKey);
	}

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(optional = false)
	private UserEntity user;

	@Column(nullable = true)
	private String deviceId; // unique device id, such as ISME or MAC address

	@Column(nullable = false)
	private ActivationStatus status;

	@Column(nullable = true)
	private String publicKey;

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
