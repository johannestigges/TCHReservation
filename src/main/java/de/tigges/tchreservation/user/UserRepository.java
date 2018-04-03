package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Long> {
    Optional<User> findByNameOrEmail(String userName, String email);
}
