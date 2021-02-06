package de.tigges.tchreservation.user;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserAwareService {

	protected final UserRepository userRepository;

	public UserEntity getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
			return UserUtils.anonymous();
		}
		String name = authentication.getName();
		return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
	}
}
