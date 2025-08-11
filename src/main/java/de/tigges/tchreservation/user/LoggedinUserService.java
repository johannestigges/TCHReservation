package de.tigges.tchreservation.user;

import de.tigges.tchreservation.util.exception.AuthorizationException;
import de.tigges.tchreservation.util.exception.ErrorCode;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.util.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggedinUserService {

    private final UserRepository userRepository;
    private final Validator validator;

    public UserEntity getLoggedInUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return UserUtils.anonymous();
        }
        var name = authentication.getName();
        return userRepository.findByNameOrEmail(name, name).orElse(UserUtils.anonymous());
    }

    public UserEntity verifyIsLoggedIn() {
        var user = getLoggedInUser();
        if (UserUtils.hasRole(user, UserRole.ANONYMOUS)) {
            throw new AuthorizationException(validator.messageUtil,ErrorCode.USER_NOT_LOGGED_IN);
        }
        return user;
    }

    public UserEntity verifyHasRole(UserRole... roles) {
        var loggedInUser = getLoggedInUser();
        if (!UserUtils.isActive(loggedInUser) || !UserUtils.hasRole(loggedInUser, roles)) {
            throw new AuthorizationException(validator.messageUtil,ErrorCode.USER_NOT_AUTHORIZED);
        }
        return loggedInUser;
    }

    public UserEntity verifyHasRoleOrSelf(Long userId, UserRole... roles) {
        var loggedInUser = getLoggedInUser();
        if (UserUtils.isActive(loggedInUser) &&
                (UserUtils.is(loggedInUser, userId) || UserUtils.hasRole(loggedInUser, roles))) {
            return loggedInUser;
        }
        throw new AuthorizationException(validator.messageUtil,ErrorCode.USER_NOT_AUTHORIZED);
    }
}
