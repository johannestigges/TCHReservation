package de.tigges.tchreservation.user.model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
public class User {

    private long id;

    @Nullable
    private String email;

    @Nonnull
    private String name;

    @Nullable
    private String password;

    @Nonnull
    private UserRole role;

    @Nonnull
    private ActivationStatus status;

    @Getter
    private Set<UserDevice> devices = new HashSet<>();

    public User(@Nullable String email, @Nonnull String name, @Nullable String password, @Nonnull UserRole role, @Nonnull ActivationStatus status) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public User(User user) {
        this(user.getEmail(), user.getName(), user.getPassword(), user.getRole(), user.getStatus());
        setId(user.getId());
    }

    public void setStatus(ActivationStatus status) {
        ActivationStatus.checkStatusChange(this.status, status, "user " + id);
        this.status = status;
    }
}
