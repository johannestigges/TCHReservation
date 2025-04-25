package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

class ReservationValidatorTest extends ValidatorTest {
    static final long SYSTEM_CONFIG_ID = 100L;
    static final int TYPE = 2345;

    private final OccupationRepository occupationRepositoryMock = mock(OccupationRepository.class);

    private ReservationValidator reservationValidator;

    @BeforeEach
    void initValidator() {
        OccupationValidator occupationValidator = new OccupationValidator(occupationRepositoryMock, createValidator());
        reservationValidator = new ReservationValidator(occupationValidator, createValidator());
    }

    @Test
    void noCourt() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setCourts(null);
        checkReservationFieldErrorNullNotAllowed(reservation, user, systemConfig, "court");
    }

    @Test
    void noDate() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setDate(null);
        checkReservationFieldErrorNullNotAllowed(reservation, user, systemConfig, "date");
    }

    @Test
    void noStart() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setStart(null);
        checkReservationFieldErrorNullNotAllowed(reservation, user, systemConfig, "start");

    }

    private void checkReservationFieldErrorNullNotAllowed(Reservation reservation, UserEntity user, ReservationSystemConfig systemConfig, String expectedField) {
        checkFieldError(() ->
                reservationValidator.validateReservation(reservation, user, systemConfig), ErrorCode.NULL_NOT_ALLOWED, expectedField);
    }

    private Reservation createReservation() {
        var r = new Reservation();
        r.setCourts("1");
        r.setDate(LocalDate.now().plusDays(1));
        r.setStart(LocalTime.of(10, 0));
        return r;
    }


    private UserEntity createUser(UserRole role) {
        var user = new UserEntity();
        user.setStatus(ActivationStatus.ACTIVE);
        user.setName("JUnit user");
        user.setRole(role);
        return user;
    }

    private ReservationSystemConfig createSystemConfig(int durationUnits, SystemConfigReservationType... types) {
        return new ReservationSystemConfig(
                SYSTEM_CONFIG_ID,
                "",
                "",
                List.of("1", "2", "3", "4", "5", "6"),
                durationUnits,
                0,
                0,
                8,
                22,
                Arrays.asList(types)
        );
    }

    private SystemConfigReservationType createType(int maxDuration, UserRole... roles) {
        return new SystemConfigReservationType(
                TYPE,
                "Type" + TYPE,
                maxDuration,
                0,
                0,
                true,
                true,
                Collections.emptyList(),
                null,
                List.of(roles));
    }

    private SystemConfigReservationType createType(UserRole... roles) {
        return createType(0, roles);
    }
}
