package de.tigges.tchreservation.user;

import java.util.stream.Stream;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

/**
 * static helper Methods
 * 
 * @author johannes
 */
public class UserUtils {

	private UserUtils() {
		// avoid instantiation
	}

	public static UserEntity anonymous() {
		return new UserEntity("", "", "", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
	}

	public static boolean hasRole(UserRole userRole, UserRole... roles) {
		return Stream.of(roles).anyMatch(r -> r.equals(userRole));
	}
}
