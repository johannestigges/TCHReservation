package de.tigges.tchreservation.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
public class SpringUserDetailsServiceTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SpringUserDetailsService service;

	@Before
	public void setup() throws Exception {
		userRepository.deleteAll();
	}

	@Test
	public void allRoles() {
		createAndLoadUser(UserRole.ADMIN, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(UserRole.ANONYMOUS, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(UserRole.KIOSK, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(UserRole.TRAINER, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
	}
	
	@Test
	public void allStatus() {
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.CREATED).assertDisabled().assertNotLocked();
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.ACTIVE).assertEnabled().assertNotLocked();
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.LOCKED).assertDisabled().assertLocked();
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.REMOVED).assertDisabled().assertNotLocked();
		createAndLoadUser(UserRole.REGISTERED, ActivationStatus.VERIFIED_BY_USER).assertDisabled().assertNotLocked();
	}
	
	@Test
	public void findByEmail() {
		userRepository.save(new User("my@email.de", "user", "mypassword", UserRole.REGISTERED, ActivationStatus.ACTIVE));
		service.loadUserByUsername("my@email.de");
	}
	
	@Test(expected= UsernameNotFoundException.class)
	public void unknownUser() {
		service.loadUserByUsername("unknownuser");
	}
	private UserChecker createAndLoadUser(UserRole role, ActivationStatus status) {
		String username = role.name() + "." + status.name();
		userRepository.save(new User("my@email.de", username, "mypassword", role, status));
		return new UserChecker(service.loadUserByUsername(username));
	}
	
	private class UserChecker {
		private UserDetails userDetails;

		public UserChecker(UserDetails userDetails) {
			this.userDetails = userDetails;
		}

		public UserChecker assertEnabled() {
			assertTrue("user " + userDetails.getUsername() + " is not enabled", userDetails.isEnabled());
			return this;
		}

		public UserChecker assertDisabled() {
			assertFalse("user " + userDetails.getUsername() + " is not disabled", userDetails.isEnabled());
			return this;

		}

		public UserChecker assertNotLocked() {
			assertTrue("user " + userDetails.getUsername() + " is locked", userDetails.isAccountNonLocked());
			return this;
		}

		public UserChecker assertLocked() {
			assertFalse("user " + userDetails.getUsername() + " is not locked", userDetails.isAccountNonLocked());
			return this;
		}
	}
}
