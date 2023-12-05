package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

import java.util.Arrays;

public final class UserUtils {

	public static boolean is(UserEntity user, long userId) {
		return user.getId() == userId;
	}

	public static UserEntity anonymous() {
		return new UserEntity("", "", "", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
	}

	public static boolean hasRole(UserEntity user, UserRole... roles) {
		return hasRole(user.getRole(), roles);
	}

	public static boolean hasRole(UserRole userRole, UserRole... roles) {
		return Arrays.asList(roles).contains(userRole);
	}

	public static boolean isActive(UserEntity user) {
		return ActivationStatus.ACTIVE.equals(user.getStatus());
	}
}
