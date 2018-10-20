package de.tigges.tchreservation;

import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.UserService;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

/**
 * base class for junit tests dealing with user data
 */
public class UserTest extends ServiceTest {

	@Autowired
	protected UserService userService;

	@Autowired
	protected UserRepository userRepository;

	/**
	 * add a user to database
	 * 
	 * @param role
	 * @param status
	 * @return inserted user
	 */
	public User addUser(UserRole role) {
		return userRepository.save(
				new User("myemail@mydomain.de", role.name(), 
						"mySecretPassword", role, ActivationStatus.ACTIVE));
	}
}
