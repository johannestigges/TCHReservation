package de.tigges.tchreservation.user.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class User {

	public User() {
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

	public Long getId() {
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
}
