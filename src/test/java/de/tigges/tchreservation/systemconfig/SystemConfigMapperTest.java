package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemConfigMapperTest {

    @Test
    void mapEntityToModel() {
        var entity = new SystemConfigEntity();
        entity.setId(1L);
        entity.setName("Platzbelegung");
        entity.setTitle("Court Booking");
        entity.setCourts("Platz 1,Platz 2,Platz 3");
        entity.setDurationUnitInMinutes(30);
        entity.setMaxDaysReservationInFuture(14);
        entity.setMaxDuration(3);
        entity.setOpeningHour(8);
        entity.setClosingHour(22);

        var model = SystemConfigMapper.map(entity);

        assertThat(model).isNotNull();
        assertThat(model.id()).isEqualTo(1L);
        assertThat(model.name()).isEqualTo("Platzbelegung");
        assertThat(model.title()).isEqualTo("Court Booking");
        assertThat(model.courts()).containsExactly("Platz 1", "Platz 2", "Platz 3");
        assertThat(model.durationUnitInMinutes()).isEqualTo(30);
        assertThat(model.maxDaysReservationInFuture()).isEqualTo(14);
        assertThat(model.maxDuration()).isEqualTo(3);
        assertThat(model.openingHour()).isEqualTo(8);
        assertThat(model.closingHour()).isEqualTo(22);
    }

    @Test
    void mapEntityToModelSplitsByTab() {
        var entity = new SystemConfigEntity();
        entity.setId(2L);
        entity.setName("Hallenplätze");
        entity.setCourts("Center Court\tNebenplatz");
        entity.setDurationUnitInMinutes(60);
        entity.setMaxDaysReservationInFuture(7);
        entity.setMaxDuration(2);
        entity.setOpeningHour(9);
        entity.setClosingHour(21);

        var model = SystemConfigMapper.map(entity);

        assertThat(model.courts()).containsExactly("Center Court", "Nebenplatz");
    }

    @Test
    void mapEntityToModelSplitsMixed() {
        var entity = new SystemConfigEntity();
        entity.setId(3L);
        entity.setName("Mixed");
        entity.setCourts("Platz 1, Platz 2\tPlatz 3,Platz 4");
        entity.setDurationUnitInMinutes(30);
        entity.setMaxDaysReservationInFuture(7);
        entity.setMaxDuration(2);
        entity.setOpeningHour(8);
        entity.setClosingHour(20);

        var model = SystemConfigMapper.map(entity);

        assertThat(model.courts()).containsExactly("Platz 1", "Platz 2", "Platz 3", "Platz 4");
    }

    @Test
    void mapEntityToModelNullIdBecomesZero() {
        var entity = new SystemConfigEntity();
        entity.setName("No ID");
        entity.setCourts("Court 1");
        entity.setDurationUnitInMinutes(60);
        entity.setMaxDaysReservationInFuture(7);
        entity.setMaxDuration(2);
        entity.setOpeningHour(8);
        entity.setClosingHour(20);

        var model = SystemConfigMapper.map(entity);

        assertThat(model.id()).isEqualTo(0L);
    }

    @Test
    void mapModelToEntity() {
        var model = new ReservationSystemConfig(
                5L, "Outdoor", "Outdoor Courts",
                List.of("Court A", "Court B", "Court C"),
                30, 7, 2, 8, 20, null);

        var entity = SystemConfigMapper.map(model, true);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getName()).isEqualTo("Outdoor");
        assertThat(entity.getTitle()).isEqualTo("Outdoor Courts");
        assertThat(entity.getDurationUnitInMinutes()).isEqualTo(30);
        assertThat(entity.getMaxDaysReservationInFuture()).isEqualTo(7);
        assertThat(entity.getMaxDuration()).isEqualTo(2);
        assertThat(entity.getOpeningHour()).isEqualTo(8);
        assertThat(entity.getClosingHour()).isEqualTo(20);
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void mapModelToEntityJoinsCourtsWithTab() {
        var model = new ReservationSystemConfig(
                1L, "Test", null,
                List.of("Platz 1", "Platz 2", "Platz 3"),
                30, 7, 2, 8, 20, null);

        var entity = SystemConfigMapper.map(model, false);

        assertThat(entity.getCourts()).isEqualTo("Platz 1\tPlatz 2\tPlatz 3");
    }

    @Test
    void mapModelToEntityIsNewFalse() {
        var model = new ReservationSystemConfig(
                1L, "Test", null, List.of("C1"), 60, 7, 2, 8, 22, null);

        var entity = SystemConfigMapper.map(model, false);

        assertThat(entity.isNew()).isFalse();
    }
}
