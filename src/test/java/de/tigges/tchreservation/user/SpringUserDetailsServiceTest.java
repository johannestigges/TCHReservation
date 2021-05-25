package de.tigges.tchreservation.user;

import static de.tigges.tchreservation.user.model.ActivationStatus.ACTIVE;
import static de.tigges.tchreservation.user.model.ActivationStatus.CREATED;
import static de.tigges.tchreservation.user.model.ActivationStatus.LOCKED;
import static de.tigges.tchreservation.user.model.ActivationStatus.REMOVED;
import static de.tigges.tchreservation.user.model.ActivationStatus.VERIFIED_BY_USER;
import static de.tigges.tchreservation.user.model.UserRole.ADMIN;
import static de.tigges.tchreservation.user.model.UserRole.ANONYMOUS;
import static de.tigges.tchreservation.user.model.UserRole.KIOSK;
import static de.tigges.tchreservation.user.model.UserRole.REGISTERED;
import static de.tigges.tchreservation.user.model.UserRole.TRAINER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
public class SpringUserDetailsServiceTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SpringUserDetailsService service;

	@BeforeEach
	public void setup() throws Exception {
		userRepository.deleteAll();
	}

	@Test
	public void allRoles() {
		createAndLoadUser(ADMIN, ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(ANONYMOUS, ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(KIOSK, ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(REGISTERED, ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(TRAINER, ACTIVE).assertEnabled().assertNotLocked();
	}

	@Test
	public void allStatus() {
		createAndLoadUser(REGISTERED, CREATED).assertDisabled().assertNotLocked();
		createAndLoadUser(REGISTERED, ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(REGISTERED, LOCKED).assertDisabled().assertLocked();
		createAndLoadUser(REGISTERED, REMOVED).assertDisabled().assertNotLocked();
		createAndLoadUser(REGISTERED, VERIFIED_BY_USER).assertDisabled().assertNotLocked();
	}

	@Test
	public void findByEmail() {
		userRepository.save(new UserEntity("my@email.de", "user", "mypassword", REGISTERED, ACTIVE));
		service.loadUserByUsername("my@email.de");
	}

	@Test
	public void unknownUser() {
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.loadUserByUsername("unknownuser");
		});
	}

	private UserChecker createAndLoadUser(UserRole role, ActivationStatus status) {
		String username = role.name() + "." + status.name();
		userRepository.save(new UserEntity("my@email.de", username, "mypassword", role, status));
		return new UserChecker(service.loadUserByUsername(username));
	}

	private class UserChecker {
		private UserDetails userDetails;

		public UserChecker(UserDetails userDetails) {
			this.userDetails = userDetails;
		}

		public UserChecker assertEnabled() {
			assertThat(userDetails.isEnabled()).isTrue();
			return this;
		}

		public UserChecker assertDisabled() {
			assertThat(userDetails.isEnabled()).isFalse();
			return this;
		}

		public UserChecker assertNotLocked() {
			assertThat(userDetails.isAccountNonLocked()).isTrue();
			return this;
		}

		public UserChecker assertLocked() {
			assertThat(userDetails.isAccountNonLocked()).isFalse();
			return this;
		}
	}
}