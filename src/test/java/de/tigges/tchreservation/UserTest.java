package de.tigges.tchreservation;

import de.tigges.tchreservation.user.jpa.UserDeviceRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

public class UserTest extends ServiceTest {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected UserDeviceRepository userDeviceRepository;

    public UserEntity addUser(UserRole role) {

        return userRepository.save(new UserEntity(
                "myemail@mydomain.de",
                role.name(),
                "mySecretPassword",
                role,
                ActivationStatus.ACTIVE));
    }

    public void deleteUser(UserRole role) {
        userRepository.findByNameOrEmail(role.name(), role.name())
                .ifPresent(user -> userRepository.delete(user));
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
