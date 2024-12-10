package de.tigges.tchreservation.user.jpa;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.Protocollable;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "\"user\"")
@Data
@NoArgsConstructor
public class UserEntity implements Protocollable {

	public UserEntity(String email, String name, String password, UserRole role, ActivationStatus status) {
		setEmail(email);
		setName(name);
		setPassword(password);
		setRole(role);
		setStatus(status);
	}

	public UserEntity(UserEntity user) {
		this(user.getEmail(), user.getName(), user.getPassword(), user.getRole(), user.getStatus());
		setId(user.getId());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column()
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private UserRole role;

	@Column(nullable = false)
	private ActivationStatus status;

	@Transient
	private Set<UserDeviceEntity> devices = new HashSet<>();

	public void setStatus(ActivationStatus status) {
		ActivationStatus.checkStatusChange(this.status, status, "user " + id);
		this.status = status;
	}

	@Override
	public Map<String, String> protocolFields() {
		return protocolFields(//
				"name", name, //
				"email", email, //
				"role", role.name(), //
				"status", status.name() //
		);
	}

	@Override
	public EntityType protocolEntityType() {
		return EntityType.USER;
	}

	@Override
	public long protocolEntityId() {
		return id;
	}
}
