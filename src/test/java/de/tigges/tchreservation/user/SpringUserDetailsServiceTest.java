package de.tigges.tchreservation.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
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
	public void getUser() throws Exception {
		addUser("abcdef", "1", UserRole.REGISTERED, ActivationStatus.ACTIVE);
		new UserChecker(service.loadUserByUsername("abcdef")).assertCredentialsNotExpired().assertUserNotExpired()
				.assertEnabled().assertNotLocked();
	}

	@Test
	public void getAnonymous() throws Exception {
		addUser("abcdef", "1", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
		new UserChecker(service.loadUserByUsername("abcdef")).assertCredentialsNotExpired().assertUserNotExpired()
				.assertEnabled().assertNotLocked();
	}

	@Test
	public void getUserCreated() throws Exception {
		addUser("abcdef", "1", UserRole.REGISTERED, ActivationStatus.CREATED);
		new UserChecker(service.loadUserByUsername("abcdef")).assertCredentialsNotExpired().assertUserNotExpired()
				.assertDisabled().assertNotLocked();
	}

	private User addUser(String name, String email, UserRole role, ActivationStatus status) {
		return userRepository.save(new User(email, name, "password", role, status));
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

		public UserChecker assertUserNotExpired() {
			assertTrue("user " + userDetails.getUsername() + " is expired", userDetails.isAccountNonExpired());
			return this;
		}

		public UserChecker assertUserExpired() {
			assertFalse(userDetails.isAccountNonExpired());
			return this;
		}

		public UserChecker assertCredentialsNotExpired() {
			assertTrue(userDetails.isCredentialsNonExpired());
			return this;
		}

		public UserChecker assertCredentialsExpired() {
			assertFalse(userDetails.isCredentialsNonExpired());
			return this;
		}
	}
}
