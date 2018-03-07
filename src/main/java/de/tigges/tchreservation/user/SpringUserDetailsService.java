package de.tigges.tchreservation.user;

import java.util.Collections;
import java.util.Optional;

import de.tigges.tchreservation.user.model.ActivationStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import de.tigges.tchreservation.user.model.User;

public class SpringUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	public SpringUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		return userRepository.findByName(userName)
				.map(u -> org.springframework.security.core.userdetails.User//
						.withUsername(userName) //
						.password(u.getPassword()) //
						.authorities(Collections.emptyList()) //
						.accountExpired(false) //
						.accountLocked(ActivationStatus.LOCKED.equals(u.getStatus())) //
						.credentialsExpired(false) //
						.disabled(!ActivationStatus.ACTIVE.equals(u.getStatus())) //
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("user " + userName + " not found"));
	}
}
