package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import de.tigges.tchreservation.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationValidatorTest extends ValidatorTest {
    static final int SYSTEM_CONFIG_ID = 100;
    static final int TYPE = 2345;

    private final OccupationRepository occupationRepositoryMock = mock(OccupationRepository.class);

    private ReservationValidator reservationValidator;
    private OccupationValidator occupationValidator;

    @BeforeEach
    void initValidator() {
        occupationValidator = new OccupationValidator(occupationRepositoryMock, createValidator());
        reservationValidator = new ReservationValidator(occupationValidator, createValidator());
    }

    @Test
    void noCourt() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setCourts(null);
        checkReservationFieldError(reservation,user,systemConfig,"error_null_not_allowed","court");
    }
    @Test void noDate() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setDate(null);
        checkReservationFieldError(reservation,user,systemConfig,"error_null_not_allowed","date");
    }

    @Test void noStart(){
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var reservation = createReservation();
        reservation.setStart(null);
        checkReservationFieldError(reservation,user,systemConfig,"error_null_not_allowed","start");

    }

    private void checkReservationError(Reservation reservation, UserEntity user, ReservationSystemConfig systemConfig, String expectedError) {
        initMessageSource(expectedError, "Pfui!");
        var exception = assertThrows(BadRequestException.class,
                () -> reservationValidator.validateReservation(reservation, user, systemConfig));
        assertTrue(exception.getErrorMessages().stream().anyMatch(e -> "Pfui!".equals(e.message())));
    }

    private void checkReservationFieldError(Reservation reservation, UserEntity user, ReservationSystemConfig systemConfig, String expectedError, String expectedField) {
        checkFieldError(() -> reservationValidator.validateReservation(reservation, user, systemConfig),expectedError,expectedField);
    }


    private Reservation createReservation() {
        var r = new Reservation();
        r.setCourts("1");
        r.setDate(LocalDate.now().plusDays(1));
        r.setStart(LocalTime.of(10,0));
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
                0,
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

    private SystemConfigReservationType createType(UserRole role, Collection<DayOfWeek> forbiddenDays) {
        return new SystemConfigReservationType(
                0,
                TYPE,
                "Type" + TYPE,
                0,
                0,
                0,
                true,
                true,
                forbiddenDays,
                null,
                List.of(role)
        );
    }


    private Occupation createOccupation() {
        var occupation = new Occupation();
        occupation.setSystemConfigId(SYSTEM_CONFIG_ID);
        occupation.setType(TYPE);
        occupation.setText("Text " + TYPE);
        occupation.setDate(LocalDate.now().plusDays(1));
        occupation.setStart(LocalTime.of(10, 0));
        occupation.setDuration(1);
        occupation.setCourt(1);
        return occupation;
    }
}
