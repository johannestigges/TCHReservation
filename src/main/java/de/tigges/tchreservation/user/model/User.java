package de.tigges.tchreservation.user.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class User {

	User() {
	}

	public User(String email, String name, String password, UserRole role, ActivationStatus status) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.role = role;
		this.status = status;
	}

	@Id
	@GeneratedValue
	private Long id;
	private String email;
	private String name;
	private String password;
	private UserRole role;
	private ActivationStatus status;

	@OneToMany(mappedBy = "user")
	private Set<UserDevice> devices = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public UserRole getRole() {
		return role;
	}

	public ActivationStatus getStatus() {
		return status;
	}

	public Set<UserDevice> getDevices() {
		return devices;
	}

	public void setStatus(ActivationStatus status) {
		ActivationStatus.checkStatusChange(this.status, status, "user " + id);
		this.status = status;
	}
}
