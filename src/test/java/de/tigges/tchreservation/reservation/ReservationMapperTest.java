package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.model.RepeatType;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationMapper;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    @Test
    void mapEntityNull() {
        assertThat(ReservationMapper.map((ReservationEntity) null)).isNull();
    }

    @Test
    void mapModelNull() {
        assertThat(ReservationMapper.map((Reservation) null)).isNull();
    }

    @Test
    void mapEntityToModel() {
        var entity = new ReservationEntity();
        entity.setId(1L);
        entity.setSystemConfigId(2L);
        entity.setText("Test reservation");
        entity.setDate(LocalDate.of(2026, 5, 1));
        entity.setStart(LocalTime.of(10, 0));
        entity.setDuration(2);
        entity.setCourts("1 2");
        entity.setType(0);
        var userEntity = new UserEntity("u@example.com", "testuser", "pw", UserRole.REGISTERED, ActivationStatus.ACTIVE);
        userEntity.setId(99L);
        entity.setUser(userEntity);

        var model = ReservationMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(1L);
        assertThat(model.getSystemConfigId()).isEqualTo(2L);
        assertThat(model.getText()).isEqualTo("Test reservation");
        assertThat(model.getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(model.getStart()).isEqualTo(LocalTime.of(10, 0));
        assertThat(model.getDuration()).isEqualTo(2);
        assertThat(model.getCourts()).isEqualTo("1 2");
        assertThat(model.getType()).isEqualTo(0);
        assertThat(model.getUser()).isNotNull();
        assertThat(model.getUser().getId()).isEqualTo(99L);
    }

    @Test
    void mapEntityToModelWithRepeat() {
        var entity = new ReservationEntity();
        entity.setSystemConfigId(1L);
        entity.setText("Weekly training");
        entity.setDate(LocalDate.of(2026, 5, 1));
        entity.setStart(LocalTime.of(9, 0));
        entity.setDuration(1);
        entity.setCourts("3");
        entity.setType(1);
        entity.setRepeatType(RepeatType.weekly);
        entity.setRepeatUntil(LocalDate.of(2026, 6, 1));
        entity.setUser(new UserEntity("a@b.de", "trainer", "pw", UserRole.TRAINER, ActivationStatus.ACTIVE));

        var model = ReservationMapper.map(entity);

        assertThat(model.getRepeatType()).isEqualTo(RepeatType.weekly);
        assertThat(model.getRepeatUntil()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void mapModelToEntity() {
        var user = new User("u@example.com", "testuser", null, UserRole.TRAINER, ActivationStatus.ACTIVE);
        user.setId(5L);
        var model = new Reservation(1L, user, "My reservation", "2", LocalDate.of(2026, 5, 10), LocalTime.of(14, 0), 3, 1);
        model.setId(10L);

        var entity = ReservationMapper.map(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getSystemConfigId()).isEqualTo(1L);
        assertThat(entity.getText()).isEqualTo("My reservation");
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(entity.getStart()).isEqualTo(LocalTime.of(14, 0));
        assertThat(entity.getDuration()).isEqualTo(3);
        assertThat(entity.getCourts()).isEqualTo("2");
        assertThat(entity.getType()).isEqualTo(1);
        assertThat(entity.getUser()).isNotNull();
        assertThat(entity.getUser().getId()).isEqualTo(5L);
    }

    @Test
    void mapModelToEntityWithRepeat() {
        var user = new User("a@b.de", "trainer", null, UserRole.TRAINER, ActivationStatus.ACTIVE);
        var model = new Reservation(1L, user, "Daily training", "1", LocalDate.of(2026, 5, 1), LocalTime.of(8, 0), 2, 0);
        model.setRepeatType(RepeatType.daily);
        model.setRepeatUntil(LocalDate.of(2026, 5, 7));

        var entity = ReservationMapper.map(model);

        assertThat(entity.getRepeatType()).isEqualTo(RepeatType.daily);
        assertThat(entity.getRepeatUntil()).isEqualTo(LocalDate.of(2026, 5, 7));
    }

    @Test
    void mapModelToEntityNullUser() {
        var model = new Reservation(1L, null, "text", "1", LocalDate.now(), LocalTime.of(8, 0), 1, 0);

        var entity = ReservationMapper.map(model);

        assertThat(entity.getUser()).isNull();
    }
}
