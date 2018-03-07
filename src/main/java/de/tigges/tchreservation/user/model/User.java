package de.tigges.tchreservation.user.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.GenerationType.AUTO;

@Entity
public class User {


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
	
	@OneToMany(mappedBy = "userId")
    private Set<UserDevice> devices = new HashSet<>();


    public Long getId() {
        return id;
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
    public UserRole getRole() { return role; }
    public ActivationStatus getStatus() { return status; }
    public Set<UserDevice> getDevices() {
		return devices;
	}
}
