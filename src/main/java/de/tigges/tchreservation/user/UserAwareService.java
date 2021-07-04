package de.tigges.tchreservation.user;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserAwareService {

	protected final UserRepository userRepository;

	protected UserEntity getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
			return UserUtils.anonymous();
		}
		String name = authentication.getName();
		return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
	}

	protected UserEntity verifyHasRole(UserRole... roles) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!hasRole(loggedInUser, roles)) {
			throw new AuthorizationException("error_user_is_not_admin");
		}
		return loggedInUser;
	}

	protected UserEntity verifyHasRoleOrSelf(Long userId, UserRole... roles) {
		UserEntity loggedInUser = getLoggedInUser();
		if (is(loggedInUser, userId) || hasRole(loggedInUser, roles)) {
			return loggedInUser;
		}
		throw new AuthorizationException("error_user_is_not_admin");
	}

	protected boolean hasRole(UserEntity user, UserRole... roles) {
		return UserUtils.hasRole(user.getRole(), roles) && ActivationStatus.ACTIVE.equals(user.getStatus());
	}

	protected boolean is(UserEntity user, long userId) {
		return user.getId() == userId;
	}
}
