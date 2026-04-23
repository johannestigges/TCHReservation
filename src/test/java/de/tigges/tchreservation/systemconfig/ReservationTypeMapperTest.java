package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeEntity;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeMapper;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTypeMapperTest {

    @Test
    void mapEntityToModel() {
        var entity = new ReservationTypeEntity();
        entity.setId(1L);
        entity.setType(0);
        entity.setName("Quickbuchung");
        entity.setMaxDuration(3);
        entity.setMaxDaysReservationInFuture(14);
        entity.setMaxCancelInHours(2);
        entity.setRepeatable(true);
        entity.setPublicVisible(false);
        entity.setCssStyle("highlight");
        entity.setRoles("ADMIN,REGISTERED,TRAINER");

        var model = ReservationTypeMapper.mapEntity(entity);

        assertThat(model).isNotNull();
        assertThat(model.id()).isEqualTo(1L);
        assertThat(model.type()).isEqualTo(0);
        assertThat(model.name()).isEqualTo("Quickbuchung");
        assertThat(model.maxDuration()).isEqualTo(3);
        assertThat(model.maxDaysReservationInFuture()).isEqualTo(14);
        assertThat(model.maxCancelInHours()).isEqualTo(2);
        assertThat(model.repeatable()).isTrue();
        assertThat(model.publicVisible()).isFalse();
        assertThat(model.cssStyle()).isEqualTo("highlight");
        assertThat(model.roles()).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.REGISTERED, UserRole.TRAINER);
    }

    @Test
    void mapEntityToModelWithForbiddenDays() {
        var entity = new ReservationTypeEntity();
        entity.setType(1);
        entity.setName("Training");
        entity.setRoles("ADMIN,TRAINER");
        entity.setForbiddenDaysOfWeek("SATURDAY,SUNDAY");

        var model = ReservationTypeMapper.mapEntity(entity);

        assertThat(model.forbiddenDaysOfWeek()).containsExactlyInAnyOrder(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    @Test
    void mapEntityToModelNullForbiddenDays() {
        var entity = new ReservationTypeEntity();
        entity.setType(0);
        entity.setName("Normal");
        entity.setRoles("ADMIN");
        entity.setForbiddenDaysOfWeek(null);

        var model = ReservationTypeMapper.mapEntity(entity);

        assertThat(model.forbiddenDaysOfWeek()).isEmpty();
    }

    @Test
    void mapEntityToModelNullMaxValuesDefaultToZero() {
        var entity = new ReservationTypeEntity();
        entity.setType(0);
        entity.setName("Test");
        entity.setRoles("ADMIN");
        entity.setMaxDuration(null);
        entity.setMaxDaysReservationInFuture(null);
        entity.setMaxCancelInHours(null);

        var model = ReservationTypeMapper.mapEntity(entity);

        assertThat(model.maxDuration()).isEqualTo(0);
        assertThat(model.maxDaysReservationInFuture()).isEqualTo(0);
        assertThat(model.maxCancelInHours()).isEqualTo(0);
    }

    @Test
    void mapTypeToEntity() {
        var model = new SystemConfigReservationType(
                10L, 2, "Meisterschaft", 2, 30, 4,
                false, true,
                List.of(DayOfWeek.MONDAY),
                "bold",
                List.of(UserRole.ADMIN, UserRole.TRAINER));

        var entity = ReservationTypeMapper.mapType(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getType()).isEqualTo(2);
        assertThat(entity.getName()).isEqualTo("Meisterschaft");
        assertThat(entity.getMaxDuration()).isEqualTo(2);
        assertThat(entity.getMaxDaysReservationInFuture()).isEqualTo(30);
        assertThat(entity.getMaxCancelInHours()).isEqualTo(4);
        assertThat(entity.isRepeatable()).isFalse();
        assertThat(entity.isPublicVisible()).isTrue();
        assertThat(entity.getCssStyle()).isEqualTo("bold");
        assertThat(entity.getRoles()).contains("ADMIN");
        assertThat(entity.getRoles()).contains("TRAINER");
        assertThat(entity.getForbiddenDaysOfWeek()).contains("MONDAY");
    }

    @Test
    void mapRolesFromCollection() {
        var roles = List.of(UserRole.ADMIN, UserRole.REGISTERED);
        var result = ReservationTypeMapper.mapRoles(roles);
        assertThat(result).contains("ADMIN");
        assertThat(result).contains("REGISTERED");
    }

    @Test
    void mapRolesFromString() {
        var result = ReservationTypeMapper.mapRoles("ADMIN,TRAINER,KIOSK");
        assertThat(result).containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.TRAINER, UserRole.KIOSK);
    }

    @Test
    void mapEntitiesNull() {
        assertThat(ReservationTypeMapper.mapEntities(null)).isNull();
    }

    @Test
    void mapEntities() {
        var entity1 = new ReservationTypeEntity();
        entity1.setId(1L);
        entity1.setType(0);
        entity1.setName("Type A");
        entity1.setRoles("ADMIN");

        var entity2 = new ReservationTypeEntity();
        entity2.setId(2L);
        entity2.setType(1);
        entity2.setName("Type B");
        entity2.setRoles("TRAINER");

        var result = ReservationTypeMapper.mapEntities(List.of(entity1, entity2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("Type A", "Type B");
    }

    @Test
    void mapTypesNull() {
        assertThat(ReservationTypeMapper.mapTypes(null)).isNull();
    }

    @Test
    void mapTypes() {
        var type1 = new SystemConfigReservationType(
                null, 0, "Quick", 2, 7, 0, false, true,
                Collections.emptyList(), null, List.of(UserRole.ADMIN));
        var type2 = new SystemConfigReservationType(
                null, 1, "Training", 0, 0, 0, true, false,
                Collections.emptyList(), null, List.of(UserRole.TRAINER));

        var result = ReservationTypeMapper.mapTypes(Set.of(type1, type2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("Quick", "Training");
    }

    @Test
    void mapIntegerWithValue() {
        assertThat(ReservationTypeMapper.mapInteger(5, 0)).isEqualTo(5);
    }

    @Test
    void mapIntegerNullReturnsDefault() {
        assertThat(ReservationTypeMapper.mapInteger(null, 99)).isEqualTo(99);
    }
}
