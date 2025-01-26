package de.tigges.tchreservation.reservation;

import de.tigges.tchreservation.ValidatorTest;
import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.ErrorCode;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class OccupationValidatorTest extends ValidatorTest {
    static final int SYSTEM_CONFIG_ID = 100;
    static final int TYPE = 2345;

    private final OccupationRepository occupationRepositoryMock = mock(OccupationRepository.class);

    private OccupationValidator occupationValidator;

    @BeforeEach
    void initValidator() {
        occupationValidator = new OccupationValidator(occupationRepositoryMock, createValidator());
    }

    @Test
    void noSystemConfig() {
        checkOccupationError(new Occupation(), null, null, ErrorCode.NO_RESERVATION_SYSTEM);
    }

    @Test
    void noType() {
        var occupation = new Occupation();
        occupation.setSystemConfigId(SYSTEM_CONFIG_ID);
        occupation.setType(2);
        var systemConfig = createSystemConfig(60);

        checkOccupationError(occupation, null, systemConfig, ErrorCode.INVALID_RESERVATION_TYPE);
    }

    @Test
    void noText() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setText(null);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.NULL_NOT_ALLOWED, "text");
    }

    @Test
    void noDate() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setDate(null);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.NULL_NOT_ALLOWED, "date");
    }

    @Test
    void noStart() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(null);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.NULL_NOT_ALLOWED, "start");
    }

    @Test
    void startBeforeOpeningHour() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.openingHour() - 1, 0));

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.START_HOUR_BEFORE_OPENING, "start");
    }

    @Test
    void startAfterClosingHour() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.closingHour() + 1, 0));

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.START_HOUR_AFTER_CLOSING, "start");
    }

    @Test
    void startWrongMinutes() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(30, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.openingHour(), 20));

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.START_TIME_MINUTES, "start");
    }

    @Test
    void restrictedWeekday() {
        var user = createUser(UserRole.GUEST);
        var systemConfig = createSystemConfig(30, createType(UserRole.GUEST, List.of(SUNDAY)));
        var occupation = createOccupation();
        occupation.setDate(LocalDate.now().with(next(SUNDAY)));

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.DAY_OF_WEEK_NOT_ALLOWED, "date");
    }

    @Test
    void durationAfterEnd() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(30, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setStart(LocalTime.of(systemConfig.closingHour() - 1, 30));
        occupation.setDuration(2);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.START_TIME_PLUS_DURATION, "start");
    }

    @Test
    void durationTooSmall() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setDuration(0);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.DURATION_TOO_SMALL, "duration");
    }

    @Test
    void durationTooLong() {
        var type = createType(3, UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, type);
        var user = createUser(UserRole.REGISTERED);
        var occupation = createOccupation();
        occupation.setDuration(5);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.DURATION_TOO_LONG, "duration");
    }

    @Test
    void courtTooSmall() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setCourt(0);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.COURT_TOO_SMALL, "court");
    }

    @Test
    void courtTooBig() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        occupation.setCourt(10);

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.COURT_TOO_BIG, "court");
    }

    @Test
    void userNotActive() {
        var user = createUser(UserRole.REGISTERED);
        user.setStatus(ActivationStatus.LOCKED);
        var systemConfig = createSystemConfig(60, createType(UserRole.REGISTERED));
        var occupation = createOccupation();
        initMessageSource(ErrorCode.USER_NOT_ACTIVE, "Pfui! %s");

        var exception = assertThrows(AuthorizationException.class,
                () -> occupationValidator.validateOccupation(occupation, user, systemConfig));

        assertTrue(exception.getErrorMessages().stream()
                .anyMatch(e -> "Pfui! JUnit user".equals(e.message())));
    }

    @Test
    void userNotAllowed() {
        var user = createUser(UserRole.REGISTERED);
        var systemConfig = createSystemConfig(60, createType(UserRole.ADMIN, UserRole.TEAMSTER, UserRole.TRAINER));
        var occupation = createOccupation();

        checkOccupationFieldError(occupation, user, systemConfig, ErrorCode.USER_CANNOT_ADD_TYPE, "type");
    }

    private void checkOccupationError(Occupation occupation, UserEntity user, ReservationSystemConfig systemConfig, ErrorCode expectedError) {
        checkError(
                () -> occupationValidator.validateOccupation(occupation, user, systemConfig),
                expectedError);
    }

    private void checkOccupationFieldError(Occupation occupation, UserEntity user, ReservationSystemConfig systemConfig, ErrorCode expectedError, String expectedField) {
        checkFieldError(
                () -> occupationValidator.validateOccupation(occupation, user, systemConfig),
                expectedError, expectedField);
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

    private UserEntity createUser(UserRole role) {
        var user = new UserEntity();
        user.setStatus(ActivationStatus.ACTIVE);
        user.setName("JUnit user");
        user.setRole(role);
        return user;
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
}
