package de.tigges.tchreservation.user;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * User utilities with methods and utils regarding logged in user
 *
 * @author johannes
 */
@Component
@RequiredArgsConstructor
public class LoggedinUserService {

    private final UserRepository userRepository;

    public UserEntity getLoggedInUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            return UserUtils.anonymous();
        }
        var name = authentication.getName();
        return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
    }

    public UserEntity verifyHasRole(UserRole... roles) {
        var loggedInUser = getLoggedInUser();
        if (!UserUtils.isActive(loggedInUser) || !UserUtils.hasRole(loggedInUser, roles)) {
            throw new AuthorizationException("error_user_is_not_admin");
        }
        return loggedInUser;
    }

    public UserEntity verifyHasRoleOrSelf(Long userId, UserRole... roles) {
        var loggedInUser = getLoggedInUser();
        if (UserUtils.isActive(loggedInUser) && (UserUtils.is(loggedInUser, userId) || UserUtils.hasRole(loggedInUser, roles))) {
            return loggedInUser;
        }
        throw new AuthorizationException("error_user_is_not_admin");
    }
}
