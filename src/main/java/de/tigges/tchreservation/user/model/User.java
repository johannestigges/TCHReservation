package de.tigges.tchreservation.user.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.protocol.ProtocolEntity;

@Entity
public class User implements ProtocolEntity {

	public User() {
	}

	public User(String email, String name, String password, UserRole role, ActivationStatus status) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.role = role;
		this.status = status;
	}

	public User(User user) {
		this(user.getEmail(), user.getName(), user.getPassword(), user.getRole(), user.getStatus());
		setId(user.getId());
	}

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String password;

	@Column(nullable = false)
	private UserRole role;

	@Column(nullable = false)
	private ActivationStatus status;

	@Transient
	private Set<UserDevice> devices = new HashSet<>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public ActivationStatus getStatus() {
		return status;
	}

	public void setStatus(ActivationStatus status) {
		ActivationStatus.checkStatusChange(this.status, status, "user " + id);
		this.status = status;
	}

	public Set<UserDevice> getDevices() {
		return devices;
	}

	public boolean hasRole(UserRole... roles) {
		for (UserRole role : roles) {
			if (role.equals(getRole())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toProtocol() {
		// @formatter:off
		return toProtocol("name", name, "email", email, "password", password, "role", role.name(), "status",
				status.name());
		// @formatter:on
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.USER;
	}

	@Override
	public long getEntityId() {
		return id;
	}
}
