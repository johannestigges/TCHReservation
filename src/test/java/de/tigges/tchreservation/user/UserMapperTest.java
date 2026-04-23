package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapEntityToModelNull() {
        assertThat(UserMapper.map((UserEntity) null)).isNull();
    }

    @Test
    void mapModelToEntityNull() {
        assertThat(UserMapper.map((User) null)).isNull();
    }

    @Test
    void mapEntityToModel() {
        var entity = new UserEntity("test@example.com", "testuser", "secret123", UserRole.REGISTERED, ActivationStatus.ACTIVE);
        entity.setId(42L);

        var model = UserMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(42L);
        assertThat(model.getEmail()).isEqualTo("test@example.com");
        assertThat(model.getName()).isEqualTo("testuser");
        assertThat(model.getRole()).isEqualTo(UserRole.REGISTERED);
        assertThat(model.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
    }

    @Test
    void mapEntityToModelPasswordNotMapped() {
        var entity = new UserEntity("test@example.com", "testuser", "secret123", UserRole.REGISTERED, ActivationStatus.ACTIVE);

        var model = UserMapper.map(entity);

        assertThat(model.getPassword()).isNull();
    }

    @Test
    void mapModelToEntity() {
        var model = new User("test@example.com", "testuser", "secret123", UserRole.TRAINER, ActivationStatus.ACTIVE);
        model.setId(7L);

        var entity = UserMapper.map(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getName()).isEqualTo("testuser");
        assertThat(entity.getPassword()).isEqualTo("secret123");
        assertThat(entity.getRole()).isEqualTo(UserRole.TRAINER);
        assertThat(entity.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
    }

    @Test
    void mapModelToEntityNullPassword() {
        var model = new User("test@example.com", "testuser", null, UserRole.ADMIN, ActivationStatus.ACTIVE);

        var entity = UserMapper.map(model);

        assertThat(entity.getPassword()).isNull();
    }

    @Test
    void mapEntityToModelWithNullId() {
        var entity = new UserEntity("a@b.de", "name", "pw", UserRole.ANONYMOUS, ActivationStatus.ACTIVE);
        // id not set, defaults to null

        var model = UserMapper.map(entity);

        assertThat(model.getId()).isNull();
    }
}
