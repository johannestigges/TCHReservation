package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.User;

public class UserMapper {

	private UserMapper() {
		// avoid instantiation
	}

	public static User map(UserEntity userEntity) {
		if (userEntity == null) {
			return null;
		}
		var user = new User();
		user.setId(userEntity.getId());
		user.setEmail(userEntity.getEmail());
		user.setName(userEntity.getName());
		// don't set password!!
		user.setRole(userEntity.getRole());
		user.setStatus(userEntity.getStatus());
		return user;
	}

	public static UserEntity map(User user) {
		if (user == null) {
			return null;
		}
		var userEntity = new UserEntity();
		userEntity.setId(user.getId());
		userEntity.setEmail(user.getEmail());
		userEntity.setName(user.getName());
		userEntity.setPassword(user.getPassword());
		userEntity.setRole(user.getRole());
		userEntity.setStatus(user.getStatus());
		return userEntity;
	}
}
