package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

/**
 * static helper Methods
 * 
 * @author johannes
 */
public class UserUtils {

	public static UserEntity anonymous() {
		return new UserEntity("", "", "", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
	}

	public static boolean hasRole(UserRole userRole, UserRole... roles) {
		for (UserRole role : roles) {
			if (role.equals(userRole)) {
				return true;
			}
		}
		return false;
	}
}
