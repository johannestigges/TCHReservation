package de.tigges.tchreservation.user;

import java.util.stream.Stream;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;

/**
 * User utilities with static methods and utils regarding logged in user
 * 
 * @author johannes
 */
@Component
@RequiredArgsConstructor
public class UserUtils {

	private final UserRepository userRepository;

	public UserEntity getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
			return UserUtils.anonymous();
		}
		String name = authentication.getName();
		return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
	}

	public UserEntity verifyHasRole(UserRole... roles) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isActive(loggedInUser) || !hasRole(loggedInUser, roles)) {
			throw new AuthorizationException("error_user_is_not_admin");
		}
		return loggedInUser;
	}

	public UserEntity verifyHasRoleOrSelf(Long userId, UserRole... roles) {
		UserEntity loggedInUser = getLoggedInUser();
		if (isActive(loggedInUser) && (is(loggedInUser, userId) || hasRole(loggedInUser, roles))) {
			return loggedInUser;
		}
		throw new AuthorizationException("error_user_is_not_admin");
	}

	public static boolean is(UserEntity user, long userId) {
		return user.getId() == userId;
	}

	public static UserEntity anonymous() {
		return new UserEntity("", "", "", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
	}

	public static boolean hasRole(UserEntity user, UserRole... roles) {
		return Stream.of(roles).anyMatch(r -> r.equals(user.getRole()));
	}

	public static boolean hasRole(UserRole userRole, UserRole... roles) {
		return Stream.of(roles).anyMatch(r -> r.equals(userRole));
	}

	public static boolean hasRoleOrSelf(UserEntity user, Long userId, UserRole... roles) {
		return is(user, userId) || hasRole(user, roles);
	}

	public static boolean isActive(UserEntity user) {
		return ActivationStatus.ACTIVE.equals(user.getStatus());
	}
}
