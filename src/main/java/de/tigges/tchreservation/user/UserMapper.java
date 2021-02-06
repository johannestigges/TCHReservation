package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.User;

public class UserMapper {

	private UserMapper() {
		// avoid instantiation
	}

	public static User map(UserEntity e) {
		if (e == null) {
			return null;
		}
		User u = new User();
		u.setId(e.getId());
		u.setEmail(e.getEmail());
		u.setName(e.getName());
		// don't set password!!
		u.setRole(e.getRole());
		u.setStatus(e.getStatus());
		return u;
	}

	public static UserEntity map(User u) {
		if (u == null) {
			return null;
		}
		UserEntity e = new UserEntity();
		e.setId(u.getId());
		e.setEmail(u.getEmail());
		e.setName(u.getName());
		e.setPassword(u.getPassword());
		e.setRole(u.getRole());
		e.setStatus(u.getStatus());
		return e;
	}
}
