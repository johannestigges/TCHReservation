package de.tigges.tchreservation.user;

import de.tigges.tchreservation.user.jpa.UserDeviceEntity;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDeviceMapperTest {

    @Test
    void mapEntityNull() {
        assertThat(UserDeviceMapper.map((UserDeviceEntity) null)).isNull();
    }

    @Test
    void mapModelNull() {
        assertThat(UserDeviceMapper.map((UserDevice) null)).isNull();
    }

    @Test
    void mapEntityToModel() {
        var userEntity = new UserEntity("u@example.com", "user1", "pw", UserRole.REGISTERED, ActivationStatus.ACTIVE);
        userEntity.setId(10L);
        var deviceEntity = new UserDeviceEntity(userEntity, "device-001", ActivationStatus.ACTIVE, "publicKey-abc");
        deviceEntity.setId(5L);

        var model = UserDeviceMapper.map(deviceEntity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(5L);
        assertThat(model.getDeviceId()).isEqualTo("device-001");
        assertThat(model.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(model.getPublicKey()).isEqualTo("publicKey-abc");
        assertThat(model.getUser()).isNotNull();
        assertThat(model.getUser().getId()).isEqualTo(10L);
        assertThat(model.getUser().getName()).isEqualTo("user1");
    }

    @Test
    void mapModelToEntity() {
        var user = new User("u@example.com", "user1", null, UserRole.REGISTERED, ActivationStatus.ACTIVE);
        user.setId(10L);
        var model = new UserDevice(user, "device-002", ActivationStatus.CREATED, "key-xyz");
        model.setId(8L);

        var entity = UserDeviceMapper.map(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(8L);
        assertThat(entity.getDeviceId()).isEqualTo("device-002");
        assertThat(entity.getStatus()).isEqualTo(ActivationStatus.CREATED);
        assertThat(entity.getPublicKey()).isEqualTo("key-xyz");
        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getId()).isEqualTo(10L);
        assertThat(entity.getUser().getName()).isEqualTo("user1");
    }

    @Test
    void mapEntityToModelWithNullPublicKey() {
        var userEntity = new UserEntity("u@example.com", "user1", "pw", UserRole.REGISTERED, ActivationStatus.ACTIVE);
        var deviceEntity = new UserDeviceEntity(userEntity, "device-003", ActivationStatus.CREATED, null);

        var model = UserDeviceMapper.map(deviceEntity);

        assertThat(model.getPublicKey()).isNull();
    }
}
