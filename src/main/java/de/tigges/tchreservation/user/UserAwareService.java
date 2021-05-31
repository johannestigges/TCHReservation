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

	public UserEntity getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
			return UserUtils.anonymous();
		}
		String name = authentication.getName();
		return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
	}

	protected UserEntity verifyIsAdminOrSelf(Long userId) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser) && !is(loggedInUser, userId))
			throw new AuthorizationException("not authorized");
		return loggedInUser;
	}

	protected UserEntity verifyIsAdmin() {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser)) {
			throw new AuthorizationException("error_user_is_not_admin");
		}
		return loggedInUser;
	}

	protected boolean isAdmin(UserEntity user) {
		return UserUtils.hasRole(user.getRole(), UserRole.ADMIN) && ActivationStatus.ACTIVE.equals(user.getStatus());
	}

	boolean is(UserEntity user, long userId) {
		return user.getId() == userId;
	}
}
