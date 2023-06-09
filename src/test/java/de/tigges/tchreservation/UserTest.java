package de.tigges.tchreservation;

import de.tigges.tchreservation.user.jpa.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

public class UserTest extends ServiceTest {

	@Autowired
	protected UserRepository userRepository;
	@Autowired
	protected UserDeviceRepository userDeviceRepository;

	public UserEntity addUser(UserRole role) {
		// @formatter:off
		return userRepository.save(new UserEntity(
				"myemail@mydomain.de",
				role.name(),
				"mySecretPassword",
				role,
				ActivationStatus.ACTIVE));
		// @formatter:on
	}
}
