package de.tigges.tchreservation.user.jpa;

import java.util.Map;

import jakarta.persistence.*;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(optional = false)
	private UserEntity user;

	@Column()
	private String deviceId;

	@Column(nullable = false)
	private ActivationStatus status;

	@Column()
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
