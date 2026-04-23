package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.OccupationMapper;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OccupationMapperTest {

    @Test
    void mapModelNull() {
        assertThat(OccupationMapper.map((Occupation) null)).isNull();
    }

    @Test
    void mapEntityNull() {
        assertThat(OccupationMapper.map((OccupationEntity) null)).isNull();
    }

    @Test
    void mapEntityToModel() {
        var entity = new OccupationEntity();
        entity.setId(3L);
        entity.setText("Court time");
        entity.setDate(LocalDate.of(2026, 5, 15));
        entity.setStart(LocalTime.of(11, 0));
        entity.setDuration(2);
        entity.setCourt(1);
        entity.setLastCourt(2);
        entity.setType(0);
        entity.setSystemConfigId(1L);

        var model = OccupationMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(3L);
        assertThat(model.getText()).isEqualTo("Court time");
        assertThat(model.getDate()).isEqualTo(LocalDate.of(2026, 5, 15));
        assertThat(model.getStart()).isEqualTo(LocalTime.of(11, 0));
        assertThat(model.getDuration()).isEqualTo(2);
        assertThat(model.getCourt()).isEqualTo(1);
        assertThat(model.getLastCourt()).isEqualTo(2);
        assertThat(model.getType()).isEqualTo(0);
        assertThat(model.getSystemConfigId()).isEqualTo(1L);
    }

    @Test
    void mapEntityToModelWithReservation() {
        var entity = new OccupationEntity();
        entity.setText("slot");
        entity.setDate(LocalDate.now());
        entity.setStart(LocalTime.of(10, 0));
        entity.setDuration(1);
        entity.setCourt(1);
        entity.setType(0);
        entity.setSystemConfigId(1L);

        var reservationEntity = new ReservationEntity();
        reservationEntity.setId(50L);
        reservationEntity.setSystemConfigId(1L);
        reservationEntity.setText("parent");
        reservationEntity.setDate(LocalDate.now());
        reservationEntity.setStart(LocalTime.of(10, 0));
        reservationEntity.setDuration(1);
        reservationEntity.setCourts("1");
        reservationEntity.setType(0);
        var userEntity = new UserEntity("a@b.de", "user", "pw", UserRole.REGISTERED, ActivationStatus.ACTIVE);
        reservationEntity.setUser(userEntity);
        entity.setReservation(reservationEntity);

        var model = OccupationMapper.map(entity);

        assertThat(model.getReservation()).isNotNull();
        assertThat(model.getReservation().getId()).isEqualTo(50L);
    }

    @Test
    void mapModelToEntity() {
        var occupation = new Occupation();
        occupation.setId(7L);
        occupation.setText("My slot");
        occupation.setDate(LocalDate.of(2026, 6, 1));
        occupation.setStart(LocalTime.of(8, 30));
        occupation.setDuration(3);
        occupation.setCourt(4);
        occupation.setLastCourt(6);
        occupation.setType(1);
        occupation.setSystemConfigId(2L);

        var entity = OccupationMapper.map(occupation);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getText()).isEqualTo("My slot");
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(entity.getStart()).isEqualTo(LocalTime.of(8, 30));
        assertThat(entity.getDuration()).isEqualTo(3);
        assertThat(entity.getCourt()).isEqualTo(4);
        assertThat(entity.getLastCourt()).isEqualTo(6);
        assertThat(entity.getType()).isEqualTo(1);
        assertThat(entity.getSystemConfigId()).isEqualTo(2L);
    }

    @Test
    void mapModelToEntityNullReservation() {
        var occupation = new Occupation();
        occupation.setText("slot");
        occupation.setDate(LocalDate.now());
        occupation.setStart(LocalTime.of(10, 0));
        occupation.setDuration(1);
        occupation.setCourt(1);
        occupation.setType(0);
        occupation.setSystemConfigId(1L);
        occupation.setReservation(null);

        var entity = OccupationMapper.map(occupation);

        assertThat(entity.getReservation()).isNull();
    }

    @Test
    void mapModelToEntityWithReservation() {
        var occupation = new Occupation();
        occupation.setText("slot");
        occupation.setDate(LocalDate.now());
        occupation.setStart(LocalTime.of(10, 0));
        occupation.setDuration(1);
        occupation.setCourt(1);
        occupation.setType(0);
        occupation.setSystemConfigId(1L);

        var reservation = new Reservation();
        reservation.setId(20L);
        occupation.setReservation(reservation);

        var entity = OccupationMapper.map(occupation);

        assertThat(entity.getReservation()).isNotNull();
        assertThat(entity.getReservation().getId()).isEqualTo(20L);
    }
}
