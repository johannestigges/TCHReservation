package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationValidatorTest {
    static final int SYSTEM_CONFIG_ID = 100;
    static final int TYPE = 2345;

    private final OccupationRepository occupationRepositoryMock = mock(OccupationRepository.class);
    private final MessageSource messageSourceMock = mock(MessageSource.class);

    private ReservationValidator reservationValidator;

    @BeforeEach
    void initValidator() {
        reservationValidator = new ReservationValidator(occupationRepositoryMock, messageSourceMock);
    }

    @Test
    void noSystemConfig() {
        checkOccupationError(new Occupation(), null, null, "error_no_reservation_system");
    }

    @Test
    void noType() {
        var occupation = new Occupation();
        occupation.setSystemConfigId(SYSTEM_CONFIG_ID);
        occupation.setType(2);
        var systemConfig = createSystemConfig(60);
        checkOccupationError(occupation, null, systemConfig, "error_invalid_reservation_type");
    }

    @Test
    void noText() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setText(null);
        checkOccupationFieldError(occupation, user, systemConfig, "error_null_not_allowed", "text");
    }

    @Test
    void noDate() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setDate(null);
        checkOccupationFieldError(occupation, user, systemConfig, "error_null_not_allowed", "date");
    }

    @Test
    void noStart() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(null);
        checkOccupationFieldError(occupation, user, systemConfig, "error_null_not_allowed", "start");
    }

    @Test
    void startBeforeOpeningHour() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.openingHour() - 1, 0));
        checkOccupationFieldError(occupation, user, systemConfig, "error_start_hour_before_opening", "start");
    }

    @Test
    void startAfterClosingHour() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.closingHour() + 1, 0));
        checkOccupationFieldError(occupation, user, systemConfig, "error_start_hour_after_closing", "start");
    }

    @Test
    void startWrongMinutes() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(30, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.openingHour(), 20));
        checkOccupationFieldError(occupation, user, systemConfig, "error_start_time_minutes", "start");
    }

    @Test
    void durationAfterEnd() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(30, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.closingHour() - 1, 30));
        occupation.setDuration(2);
        checkOccupationFieldError(occupation, user, systemConfig, "error_start_time_plus_duration", "start");
    }

    @Test
    void durationTooSmall() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setDuration(0);
        checkOccupationFieldError(occupation, user, systemConfig, "error_duration_too_small", "duration");
    }

    @Test
    void durationTooLong() {
        var type = createType(3, UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, type);
        var user = createUser(UserRole.REGISTERED);
        var occupation = createOccupation();
        occupation.setDuration(5);
        checkOccupationFieldError(occupation, user, systemConfig, "error_duration_too_long", "duration");
    }

    @Test
    void courtTooSmall() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setCourt(0);
        checkOccupationFieldError(occupation, user, systemConfig, "error_court_too_small", "court");
    }

    @Test
    void courtTooBig() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setCourt(10);
        checkOccupationFieldError(occupation, user, systemConfig, "error_court_too_big", "court");
    }

    @Test
    void userNotActive() {
        var user = createUser(UserRole.REGISTERED);
        user.setStatus(ActivationStatus.LOCKED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        initMessageSource("error_user_not_active", "Pfui! %s");
        var exception = assertThrows(AuthorizationException.class,
                () -> reservationValidator.validateOccupation(occupation, user, systemConfig));
        assertTrue(exception.getErrorMessages().stream()
                .anyMatch(e -> "Pfui! JUnit user".equals(e.message())));
    }

    @Test
    void userNotAllowed() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.ADMIN, UserRole.TEAMSTER, UserRole.TRAINER));
        var occupation = createOccupation();
        checkOccupationFieldError(occupation, user, systemConfig, "error_user_cannot_add_type", "type");
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
                SYSTEM_CONFIG_ID, "", "", List.of("1", "2", "3", "4", "5", "6"),
                durationUnits, 0, 0, 8, 22,
                Arrays.asList(types)
        );
    }

    private SystemConfigReservationType createType(int maxDuration, UserRole... roles) {
        return new SystemConfigReservationType(0, TYPE, "Type" + TYPE, maxDuration, 0, 0, true, true, List.of(roles));
    }

    private SystemConfigReservationType createType(UserRole... roles) {
        return createType(0, roles);
    }

    private void checkOccupationError(Occupation occupation, UserEntity user, ReservationSystemConfig systemConfig, String expectedError) {
        initMessageSource(expectedError, "Pfui!");
        var exception = assertThrows(BadRequestException.class,
                () -> reservationValidator.validateOccupation(occupation, user, systemConfig));
        assertTrue(exception.getErrorMessages().stream().anyMatch(e -> "Pfui!".equals(e.message())));
    }

    private void checkOccupationFieldError(Occupation occupation, UserEntity user, ReservationSystemConfig systemConfig, String expectedError, String expectedField) {
        initMessageSource(expectedError, "Pfui!");
        var exception = assertThrows(BadRequestException.class,
                () -> reservationValidator.validateOccupation(occupation, user, systemConfig));
        assertTrue(exception.getErrorMessages()
                .stream().anyMatch(e -> "Pfui!".equals(e.message()) && expectedField.equals(e.field())));
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

    private void initMessageSource(String msg, String value) {
        when(messageSourceMock.getMessage(eq(msg), any(), eq(Locale.getDefault()))).thenReturn(value);
    }
}
