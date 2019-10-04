package de.tigges.tchreservation.user.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
	Optional<UserEntity> findByNameOrEmail(String userName, String email);
}
