package de.tigges.tchreservation.systemconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.FieldError;
import de.tigges.tchreservation.exception.InvalidDataException;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
class SystemConfigValidatorTest {

	SystemConfigValidator validator;

	@Autowired
	MessageSource messageSource;

	@BeforeEach
	void initMocks() {
		validator = new SystemConfigValidator(messageSource);
	}

	@Test
	void validateOk() {
		validator.validate(
				new ReservationSystemConfig(1, "Reservierungssystem", Arrays.asList("Court 1"), 30, 1, 2, 8, 22, null),
				admin());
	}

	@Test
	void noId() {
		assertThrows(BadRequestException.class, () -> validator.validate(
				new ReservationSystemConfig(0, "Reservierungssystem", Arrays.asList("Court 1"), 30, 1, 2, 8, 22, null),
				admin()));
	}

	@Test
	void notAdmin() {
		assertThrows(AuthorizationException.class,
				() -> validator.validate(new ReservationSystemConfig(1, "", null, 0, 0, 0, 0, 0, null),
						new UserEntity("egal", "trainer", "", UserRole.TRAINER, ActivationStatus.ACTIVE)));
	}

	@Test
	void adminNotActive() {
		assertThrows(AuthorizationException.class,
				() -> validator.validate(new ReservationSystemConfig(1, "", null, 0, 0, 0, 0, 0, null),
						new UserEntity("egal", "trainer", "", UserRole.ADMIN, ActivationStatus.VERIFIED_BY_USER)));
	}

	@Test
	void nameTooShort() {
		assertFieldError(new ReservationSystemConfig(100, "R", Arrays.asList("Court 1"), 30, 1, 2, 8, 22, "title"),
				"name", "Bitte geben Sie einen Wert an");
	}

	@Test
	void noCourts() {
		assertFieldError(new ReservationSystemConfig(1000, "res1", Collections.emptyList(), 30, 1, 2, 8, 22, null),
				"courts", "Bitte geben Sie einen Wert an");
	}

	@Test
	void moreThanMaxCourts() {
		assertFieldError(new ReservationSystemConfig(1000, "res1",
				Arrays.asList("Pl1", "Pl2", "PL3", "PL4", "PL5", "PL6", "PL7", "Pl8", "Pl9", "PL10", "PL11", "PL12",
						"PL13", "Pl14", "Pl15", "Pl16", "Pl17", "PL18", "PL19", "Pl20", "Pl21"),
				30, 1, 2, 8, 22, null), "courts", "zu viele Plätze angegeben");
	}

	@Test
	void courtNameTooShort() {
		assertFieldError(new ReservationSystemConfig(1000, "res1", Arrays.asList("Pl1", "P2"), 30, 1, 2, 8, 22, null),
				"court", "Bitte geben Sie einen Wert an");
	}

	@Test
	void durationUnitsTooSmall() {
		assertFieldError(new ReservationSystemConfig(1000, "res1", Arrays.asList("PL1"), 29, 1, 2, 8, 22, null),
				"durationUnitInMinutes", "Wert zu klein");
	}

	@Test
	void durationUnitsTooBig() {
		assertFieldError(new ReservationSystemConfig(1000, "res1", Arrays.asList("PL1"), 61, 1, 2, 8, 22, null),
				"durationUnitInMinutes", "Wert zu groß");
	}

	@Test
	void openingHourAfterClosingHour() {
		assertFieldError(new ReservationSystemConfig(1, "res1", Arrays.asList("Pl1"), 8, 1, 2, 15, 14, null),
				"openingHour", "Öffnungszeit darf nicht später als Schließzeit sein");
	}

	private static void assertFieldError(InvalidDataException exception, String field, String message) {
		Optional<FieldError> fieldError = exception.getErrorDetails().getFieldErrors().stream()
				.filter(e -> e.getField().equals(field)).findFirst();
		assertThat(fieldError).isPresent();
		assertThat(fieldError.get().getMessage()).isEqualTo(message);
	}

	private void assertFieldError(ReservationSystemConfig config, String field, String message) {
		assertFieldError(assertThrows(InvalidDataException.class, () -> validator.validate(config, admin())), field,
				message);
	}

	private static UserEntity admin() {
		return new UserEntity("egal", "admin", "", UserRole.ADMIN, ActivationStatus.ACTIVE);
	}
}
