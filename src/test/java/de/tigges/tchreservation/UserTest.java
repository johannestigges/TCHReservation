package de.tigges.tchreservation;

import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.UserService;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

public class UserTest extends ServiceTest {

	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	public User addUser(UserRole role, ActivationStatus status) {
		String userName = role.name();
		if (!ActivationStatus.ACTIVE.equals(status)) {
			userName = userName + "." + status.name();
		}
		User user = new User("myemail@mydomain.de", userName, "mySecretPassword", role, status);
		return userRepository.save(user);
	}
}
