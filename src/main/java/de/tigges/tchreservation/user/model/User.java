package de.tigges.tchreservation.user.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class User {

	private long id;

	@Nullable
	private String email;

	@Nonnull
	private String name;

	@Nullable
	private String password;

	@Nonnull
	private UserRole role;

	@Nonnull
	private ActivationStatus status;

	private Set<UserDevice> devices = new HashSet<>();

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

	public void setStatus(ActivationStatus status) {
		ActivationStatus.checkStatusChange(this.status, status, "user " + id);
		this.status = status;
	}

	public Set<UserDevice> getDevices() {
		return devices;
	}

	public void setDevices(Set<UserDevice> devices) {
		this.devices = devices;
	}
}
