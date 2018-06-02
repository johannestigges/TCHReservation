package de.tigges.tchreservation.user.model;

import java.util.HashSet;
import java.util.Optional;
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

	public User(long id) {
		this.id = id;
	}

	public User(String id) {
		this.id = Integer.parseInt(id);
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

	public static User anonymous() {
		return new User("", "", "", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
	}

	public User hidePassword() {
		this.password = null;
		return this;
	}
	public static Optional<User> hidePassword(Optional<User> user) {
		if (user.isPresent()) {
			user.get().hidePassword();
		}
		return user;
	}

	public static Iterable<User> hidePasswords(Iterable<User> users) {
		if (users != null) {
			users.forEach(User::hidePassword);
		}
		return users;
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
		return toProtocol("name", name, "email", email, "password", password, "role", role.name(), "status",
				status.name());
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
