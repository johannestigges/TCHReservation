package de.tigges.tchreservation.systemconfig;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.util.exception.AuthorizationException;
import de.tigges.tchreservation.util.exception.BadRequestException;
import de.tigges.tchreservation.util.exception.InvalidDataException;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
class SystemConfigValidatorTest {
    @Autowired
    SystemConfigValidator validator;

    private static SystemConfigReservationType createType(int type, String name) {
        return new SystemConfigReservationType(type, name, 0, 0, 0, true, true, Collections.emptyList(), null,Collections.emptyList());
    }

    private static SystemConfigReservationType createType(int type) {
        return createType(type, "type " + type);
    }

    private static void assertFieldError(InvalidDataException exception, String field, String message) {
        assertTrue(exception.getErrorMessages().stream()
                .anyMatch(e -> message.equals(e.message()) && field.equals(e.field())));
    }

    private static UserEntity admin() {
        return new UserEntity("egal", "admin", "", UserRole.ADMIN, ActivationStatus.ACTIVE);
    }

    @Test
    void validateOk() {
        validator.validate(
                new ReservationSystemConfig(1L, "Reservierungssystem", null,
                        List.of("Court 1"), 30, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                admin());
    }

    @Test
    void noId() {
        assertThrows(BadRequestException.class, () -> validator.validate(
                new ReservationSystemConfig(0L, "Reservierungssystem", null,
                        List.of("Court 1"), 30, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                admin()));
    }

    @Test
    void notAdmin() {
        assertThrows(AuthorizationException.class,
                () -> validator.validate(new ReservationSystemConfig(1L, "", null, null, 0, 0, 0, 0, 0,
                                List.of(createType(1), createType(2), createType(3), createType(4))
                        ),
                        new UserEntity("egal", "trainer", "", UserRole.TRAINER, ActivationStatus.ACTIVE)));
    }

    @Test
    void adminNotActive() {
        assertThrows(AuthorizationException.class,
                () -> validator.validate(new ReservationSystemConfig(1L, "", null, null, 0, 0, 0, 0, 0,
                                List.of(createType(1), createType(2), createType(3), createType(4))
                        ),
                        new UserEntity("egal", "trainer", "", UserRole.ADMIN, ActivationStatus.VERIFIED_BY_USER)));
    }

    @Test
    void nameTooShort() {
        assertFieldError(new ReservationSystemConfig(100L, "R", null, List.of("Court 1"), 30, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ), "name",
                "Bitte geben Sie mindestens 3 Zeichen ein");
    }

    @Test
    void nameTooLong() {
        assertFieldError(new ReservationSystemConfig(100L, "1234567890123456789012345678901234567890123456789012345678901", null, List.of("Court 1"), 30, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ), "name",
                "Bitte geben Sie höchstens 50 Zeichen ein");
    }

    @Test
    void noCourts() {
        assertFieldError(new ReservationSystemConfig(1000L, "res1", null, Collections.emptyList(), 30, 1, 2, 8, 22, List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                "courts",
                "Bitte geben Sie einen Wert an");
    }

    @Test
    void moreThanMaxCourts() {
        assertFieldError(new ReservationSystemConfig(1000L, "res1", null,
                Arrays.asList("Pl1", "Pl2", "PL3", "PL4", "PL5", "PL6", "PL7", "Pl8", "Pl9", "PL10", "PL11", "PL12",
                        "PL13", "Pl14", "Pl15", "Pl16", "Pl17", "PL18", "PL19", "Pl20", "Pl21"),
                30, 1, 2, 8, 22, List.of(createType(1), createType(2), createType(3), createType(4))
        ), "courts", "zu viele Plätze angegeben");
    }

    @Test
    void courtNameTooShort() {
        assertFieldError(new ReservationSystemConfig(1000L, "res1", null, Arrays.asList("Pl1", "P2"), 30, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                "court", "Bitte geben Sie mindestens 3 Zeichen ein");
    }

    @Test
    void durationUnitsTooSmall() {
        assertFieldError(new ReservationSystemConfig(1000L, "res1", null, List.of("PL1"), 29, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                "durationUnitInMinutes", "Wert zu klein");
    }

    @Test
    void durationUnitsTooBig() {
        assertFieldError(new ReservationSystemConfig(1000L, "res1", null, List.of("PL1"), 61, 1, 2, 8, 22,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                "durationUnitInMinutes", "Wert zu groß");
    }

    @Test
    void openingHourAfterClosingHour() {
        assertFieldError(new ReservationSystemConfig(1L, "res1", null, List.of("Pl1"), 8, 1, 2, 15, 14,
                        List.of(createType(1), createType(2), createType(3), createType(4))
                ),
                "openingHour",
                "Öffnungszeit darf nicht später als Schließzeit sein");
    }

    @Test
    void noTypes() {
        assertFieldError(new ReservationSystemConfig(1L, "res1", null, List.of("Pl1"), 8, 1, 2, 8, 22,
                        Collections.emptyList()),
                "reservationTypes",
                "Bitte Reservierungstypen hinzufügen");
    }

    @Test
    void typeWithoutName() {
        assertFieldError(new ReservationSystemConfig(1L, "res1", null, List.of("Pl1"), 8, 1, 2, 8, 22,
                        List.of(createType(1, null))),
                "reservationTypes",
                "Bitte geben Sie einen Wert an");
    }

    private void assertFieldError(ReservationSystemConfig config, String field, String message) {
        assertFieldError(assertThrows(InvalidDataException.class, () -> validator.validate(config, admin())), field,
                message);
    }
}
