package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpringUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        return userRepository
                .findByNameOrEmail(userName, userName)
                .map(userEntity -> map(userEntity, userName))
                .orElseThrow(() -> new UsernameNotFoundException("user '%s' not found".formatted(userName)));
    }

    private UserDetails map(UserEntity userEntity, String userName) {
        return org.springframework.security.core.userdetails.User
                .withUsername(userName)
                .password(userEntity.getPassword())
                .authorities(toAuthorities(userEntity))
                .accountExpired(false)
                .accountLocked(ActivationStatus.LOCKED.equals(userEntity.getStatus()))
                .credentialsExpired(false)
                .disabled(!ActivationStatus.ACTIVE.equals(userEntity.getStatus()))
                .build();
    }
    private Collection<? extends GrantedAuthority> toAuthorities(UserEntity userEntity) {
        return List.of(new SimpleGrantedAuthority(userEntity.getRole().name()));
    }
}
