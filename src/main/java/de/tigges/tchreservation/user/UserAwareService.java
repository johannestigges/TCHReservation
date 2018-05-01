package de.tigges.tchreservation.user;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tigges.tchreservation.user.model.User;

public class UserAwareService {

	protected UserRepository userRepository;

	public UserAwareService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
			return User.anonymous();
		}
		String name = authentication.getName();
		return userRepository.findByNameOrEmail(name, name).orElse(User.anonymous());
	}
}
