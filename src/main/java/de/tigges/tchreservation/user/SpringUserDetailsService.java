package de.tigges.tchreservation.user;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;

@Component
public class SpringUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;
	private PasswordEncoder encoder;

	public SpringUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
		encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		return userRepository.findByNameOrEmail(userName,userName)
				.map(u -> org.springframework.security.core.userdetails.User//
						.withUsername(userName) //
						.password("{bcrypt}" + u.getPassword()) //
						.authorities(Arrays.asList(new SimpleGrantedAuthority(u.getRole().name()))) //
						.accountExpired(false) //
						.accountLocked(ActivationStatus.LOCKED.equals(u.getStatus())) //
						.credentialsExpired(false) //
						.disabled(!ActivationStatus.ACTIVE.equals(u.getStatus())) //
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("user '" + userName + "' not found"));
	}
}
