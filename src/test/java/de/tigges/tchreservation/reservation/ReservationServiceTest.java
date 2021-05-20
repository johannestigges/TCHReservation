package de.tigges.tchreservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.StreamSupport;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationRepository;
import de.tigges.tchreservation.reservation.jpa.ReservationEntity;
import de.tigges.tchreservation.reservation.jpa.ReservationRepository;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.OccupationMapper;
import de.tigges.tchreservation.reservation.model.RepeatType;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationMapper;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserMapper;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.UserRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
public class ReservationServiceTest extends ProtocolTest {

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private OccupationRepository occupationRepository;

	@Autowired
	ObjectMapper objectMapper;

	private UserEntity user;
	private UserEntity admin;

	@BeforeEach
	public void setup() throws Exception {

		this.protocolRepository.deleteAll();
		this.occupationRepository.deleteAll();
		this.reservationRepository.deleteAll();
		this.userRepository.deleteAll();
		user = addUser(UserRole.REGISTERED);
		admin = addUser(UserRole.ADMIN);
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservation() throws Exception {
		addReservation(createReservation(1, 1, 10, 2));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationWithUser() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationSystems() throws Exception {
		addReservation(createReservation(1, 1, 10, 2));
		addReservation(createReservation(2, 1, 10, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourts() throws Exception {
		addReservation(createReservation(1, 1, 10, 2));
		addReservation(createReservation(1, 2, 10, 2));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationTimes() throws Exception {
		addReservation(createReservation(1, 1, 10, 2));
		addReservation(createReservation(1, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap() throws Exception {
		addReservation(createReservation(1, 1, 10, 3));
		addReservationOverlap(createReservation(1, 1, 11, 2), 0);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDuplicate() throws Exception {
		addReservation(createReservation(1, 1, 10, 3));
		addReservationOverlap(createReservation(1, 1, 10, 3), 0);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap2() throws Exception {
		addReservation(createReservation(1, 1, 10, 6));
		addReservationOverlap(createReservation(1, 1, 11, 2), 0);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap3() throws Exception {
		addReservation(createReservation(1, 1, 11, 2));
		addReservationOverlap(createReservation(1, 1, 10, 6), 0);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNotOverlapDifferentSystemConfig() throws Exception {
		addReservation(createReservation(1, 1, 11, 2));
		addReservation(createReservation(2, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNotOverlapDifferentDays() throws Exception {
		Reservation reservation = createReservation(1, 1, 11, 2);
		reservation.setDate(reservation.getDate().plusDays(1));
		addReservation(reservation);
		addReservation(createReservation(1, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoText() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setText(null);
		addReservationWithOccupationFieldError(reservation, "text", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoDate() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setDate(null);
		addReservationWithFieldError(reservation, "date", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoStart() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setStart(null);
		addReservationWithFieldError(reservation, "start", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationBeforeOpeningHour() throws Exception {
		Reservation reservation = createReservation(1, 1, 6, 2);
		addReservationWithOccupationFieldError(reservation, "start",
				"Startzeit 06:00 liegt vor der Öffnungszeit 08:00.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationAfterClosingHour() throws Exception {
		Reservation reservation = createReservation(1, 1, 23, 2);
		addReservationWithOccupationFieldError(reservation, "start",
				"Startzeit 23:00 liegt später als die Endezeit 22:00");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationInvalidMinutes() throws Exception {
		Reservation reservation = createReservation(1, 1, 0, 2);
		reservation.setStart(LocalTime.of(9, 25));
		addReservationWithOccupationFieldError(reservation, "start", "fehlerhafte Startzeit mit 25 Minuten.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationExtendsClosingHour() throws Exception {
		Reservation reservation = createReservation(1, 1, 19, 7);
		addReservationWithOccupationFieldError(reservation, "start", "Startzeit + Dauer zu spät.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDurationTooSmall() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 0);
		addReservationWithOccupationFieldError(reservation, "duration", "Dauer zu gering");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationDurationTooBig() throws Exception {
		Reservation reservation = createReservation(3, 1, 8, 4);
		reservation.setDate(reservation.getDate().plusDays(1));
		addReservationWithOccupationFieldError(reservation, "duration",
				"Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung der Dauer 4 anzulegen.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDurationNotTooBigForAdmin() throws Exception {
		Reservation reservation = createReservation(3, 1, 8, 4);
		reservation.setDate(reservation.getDate().plusDays(1));
		addReservation(reservation);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoCourts() throws Exception {
		Reservation reservation = createReservation(1, 0, 8, 2);
		reservation.setCourts(null);
		addReservationWithFieldError(reservation, "court", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationTooManyCourts() throws Exception {
		Reservation reservation = createReservation(1, 0, 8, 2);
		reservation.setCourtsFromInteger(1, 2, 3, 4, 5, 6, 7);
		addReservationWithFieldError(reservation, "court", "Platznummer 7 zu groß. Mehr als 6 Plätze gibt es nicht.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourtTooSmall() throws Exception {
		Reservation reservation = createReservation(1, 0, 8, 2);
		addReservationWithFieldError(reservation, "court", "Platznummer 0 zu klein");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourtTooBig() throws Exception {
		Reservation reservation = createReservation(1, 7, 8, 2);
		addReservationWithFieldError(reservation, "court", "Platznummer 7 zu groß. Mehr als 6 Plätze gibt es nicht.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationMultipleCourts() throws Exception {
		checkReservationMultipleCourts(8, 1, 1, 2);
		checkReservationMultipleCourts(9, 1, 1, 2, 3, 4, 5, 6);
		checkReservationMultipleCourts(10, 2, 1, 3);
		checkReservationMultipleCourts(11, 2, 1, 2, 4, 5, 6);
		checkReservationMultipleCourts(12, 3, 2, 4, 6);
	}

	private void checkReservationMultipleCourts(int start, int expectedOccupations, int... courts) throws Exception {
		Reservation reservation = createReservation(1, 1, start, 2);
		reservation.setCourtsFromInteger(courts);
		Reservation savedReservation = getReservation(addReservation(reservation));
		Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
		assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDailyRepeat() throws Exception {
		checkReservationRepeatDaily(8, 3, 4, 2);
		checkReservationRepeatDaily(8, 1, 2, 3);
	}

	private void checkReservationRepeatDaily(int hour, int repeatDays, int expectedOccupations, int... courts)
			throws Exception {
		Reservation reservation = createReservation(1, 1, hour, 2);
		reservation.setRepeatUntil(reservation.getDate().plusDays(repeatDays));
		reservation.setRepeatType(RepeatType.daily);
		reservation.setCourtsFromInteger(courts);
		Reservation savedReservation = getReservation(addReservation(reservation));
		Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
		assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationWeeklyRepeat() throws Exception {
		checkReservationRepeatWeekly(8, 2, 1, 1);
		checkReservationRepeatWeekly(8, 7, 2, 2);
		checkReservationRepeatWeekly(8, 21, 8, 3, 5);
		checkReservationRepeatWeekly(9, 20, 6, 3, 5);
		checkReservationRepeatWeekly(10, 21, 8, 3, 5);
		checkReservationRepeatWeekly(11, 21, 8, 3, 5);
	}

	private void checkReservationRepeatWeekly(int hour, int repeatDays, int expectedOccupations, int... courts)
			throws Exception {
		Reservation reservation = createReservation(1, 1, hour, 2);
		reservation.setRepeatUntil(reservation.getDate().plusDays(repeatDays));
		reservation.setRepeatType(RepeatType.weekly);
		reservation.setCourtsFromInteger(courts);
		Reservation savedReservation = getReservation(addReservation(reservation));
		Iterable<OccupationEntity> occupations = occupationRepository.findByReservationId(savedReservation.getId());
		assertThat(StreamSupport.stream(occupations.spliterator(), false)).hasSize(expectedOccupations);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoRepeatUntil() throws Exception {
		Reservation reservation = createReservation(1, 3, 12, 2);
		reservation.setRepeatType(RepeatType.daily);
		addReservationWithFieldError(reservation, "repeatUntil", "Kein Datum Wiederholung bis angegeben");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationRepeatUntilBeforeStart() throws Exception {
		Reservation reservation = createReservation(1, 3, 12, 2);
		reservation.setRepeatType(RepeatType.daily);
		reservation.setRepeatUntil(reservation.getDate().minusDays(1));
		addReservationWithFieldError(reservation, "repeatUntil",
				"Datum Wiederholung bis muss hinter dem Reservierungsdatum liegen");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNull() throws Exception {
		addReservationNoCheck(null).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoSystemConfig() throws Exception {
		addReservationError(createReservation(0, 1, 10, 6), HttpStatus.NOT_FOUND,
				"SYSTEM_CONFIGURATION with id 0 not found");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationUnknownSystemConfig() throws Exception {
		addReservationError(createReservation(10, 1, 10, 6), HttpStatus.NOT_FOUND,
				"SYSTEM_CONFIGURATION with id 10 not found");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationUnknownUserName() throws Exception {
		UserEntity unknownUser = new UserEntity();
		unknownUser.setName("the unknown user");
		unknownUser.setRole(UserRole.ADMIN);
		unknownUser.setStatus(ActivationStatus.ACTIVE);
		addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
				"Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationWrongRole() throws Exception {
		UserEntity unknownUser = new UserEntity();
		unknownUser.setName(user.getName());
		unknownUser.setRole(UserRole.ADMIN);
		unknownUser.setStatus(ActivationStatus.ACTIVE);
		addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
				"Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationWrongStatus() throws Exception {
		UserEntity unknownUser = new UserEntity();
		unknownUser.setName(user.getName());
		unknownUser.setRole(UserRole.REGISTERED);
		unknownUser.setStatus(ActivationStatus.CREATED);
		addReservationError(createReservation(1, unknownUser, 1, 8, 2), HttpStatus.UNAUTHORIZED,
				"Der an der Reservierung angegebene Benutzer ist nicht der angemeldete Benutzer.");
	}

	@Test
	@WithMockUser(username = "REGISTERED.CREATED")
	public void addReservationUserInvalidStatusCreated() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.CREATED ist nicht aktiviert.");
	}

	@Test
	@WithMockUser(username = "REGISTERED.LOCKED")
	public void addReservationUserInvalidStatusLocked() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.LOCKED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.LOCKED ist nicht aktiviert.");
	}

	@Test
	@WithMockUser(username = "REGISTERED.REMOVED")
	public void addReservationUserInvalidStatusRemoved() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.REMOVED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.REMOVED ist nicht aktiviert.");
	}

	@Test
	@WithMockUser(username = "REGISTERED.VERIFIED_BY_USER")
	public void addReservationUserInvalidStatusVerified() throws Exception {
		addReservationError(
				createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.VERIFIED_BY_USER), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "Der Benutzer REGISTERED.VERIFIED_BY_USER ist nicht aktiviert.");
	}

	@Test
	@WithMockUser(username = "ANONYMOUS.CREATED")
	public void addReservationAnonymousUser() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.ANONYMOUS, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "Ohne Benutzeranmeldung können keine Reservierungen angelegt werden.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedDateInThePast() throws Exception {
		Reservation reservation = createReservation(1, 1, 10, 2);
		reservation.setDate(LocalDate.now().minusDays(1));
		addReservationWithOccupationFieldError(reservation, "date",
				"Das Datum darf nicht in der Vergangenheit liegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTimeInThePast() throws Exception {
		int hour = LocalTime.now().getHour();
		ReservationSystemConfig systemConfig = getSystemConfig(1);
		if (hour > systemConfig.getOpeningHour() + 2 && hour < systemConfig.getClosingHour() - 1) {
			Reservation reservation = createReservation(1, 1, hour - 2, 1);
			reservation.setDate(LocalDate.now());
			addReservationWithOccupationFieldError(reservation, "time",
					"Die Startzeit darf nicht in der Vergangenheit liegen.");
		}
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedInTheFarFuture() throws Exception {
		Reservation reservation = createReservation(3, 1, 8, 2);
		reservation.setDate(LocalDate.now().plusDays(3));
		addReservationWithOccupationFieldError(reservation, "date", "Das Datum liegt zu weit in der Zukunft.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypePrepaid() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setType(ReservationType.PREPAID);

		addReservationWithOccupationFieldError(reservation, //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ PREPAID anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTournament() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setType(ReservationType.TOURNAMENT);
		addReservationWithOccupationFieldError(reservation, //
				"type",
				"Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TOURNAMENT anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTrainer() throws Exception {
		Reservation reservation = createReservation(1, 1, 8, 2);
		reservation.setType(ReservationType.TRAINER);
		addReservationWithOccupationFieldError(reservation, //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TRAINER anzulegen.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateOccupation() throws Exception {
		Reservation reservation = getResponseJson(addReservation(createReservation(1, 1, 12, 2)), Reservation.class);
		Occupation occupation = reservation.getOccupations().get(0);
		occupation.setDuration(6);
		checkOccupation(updateOccupation(occupation), occupation, ActionType.MODIFY);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateOccupationNotFound() throws Exception {
		Occupation occupation = new Occupation();
		occupation.setId(0);
		updateOccupation(occupation).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateReservationNull() throws Exception {
		updateOccupation(null).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteReservation() throws Exception {
		Reservation reservation = objectMapper.readValue(addReservation(createReservation(1, 1, 10, 2))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Reservation.class);
		checkReservation(deleteReservation(reservation.getId()), reservation, ActionType.DELETE);

		assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
		assertThat(occupationRepository.findByReservationId(reservation.getId())).isEmpty();
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteReservationNotFound() throws Exception {
		deleteReservation(543636262L).andExpect(status().isNotFound());
	}

	@Test
	public void getAllNotAuthorized() throws Exception {
		performGet("/reservation/get").andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getOccupations() throws Exception {

		Reservation reservation = createReservation(1, 1, 10, 2);
		reservation.setDate(LocalDate.now());
		reservation = getReservation(addReservation(reservation));
		checkOccupations(performGet("/reservation/getOccupations/1/0").andExpect(status().isOk()),
				reservation.getOccupations());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getOccupationsWithDate() throws Exception {
		Reservation reservation = getReservation(addReservation(createReservation(1, 2, 12, 2)));
		long epochMilli = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
		checkOccupations(performGet("/reservation/getOccupations/1/" + epochMilli).andExpect(status().isOk()),
				reservation.getOccupations());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getReservation() throws Exception {
		ReservationEntity reservation = reservationRepository
				.save(ReservationMapper.map(createReservation(1, user, 1, 10, 2)));

		performGet("/reservation/get/" + reservation.getId()).andExpect(status().isOk());
	}

	private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
		return performPost("/reservation/add", reservation != null ? reservation : "");
	}

	private ResultActions updateOccupation(Occupation occupation) throws Exception {
		return performPut("/reservation/update/occupation", occupation != null ? occupation : "");
	}

	private ResultActions deleteReservation(long id) throws Exception {
		return performDelete("/reservation/delete/" + id);
	}

	private ReservationSystemConfig getSystemConfig(long id) throws Exception {
		String content = performGet("/reservation/systemconfig/" + id).andExpect(status().isOk()) //
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readValue(content, ReservationSystemConfig.class);
	}

	private ResultActions addReservation(Reservation reservation) throws Exception {
		return checkReservation(addReservationNoCheck(reservation).andExpect(status().isCreated()), reservation,
				ActionType.CREATE);
	}

	private ResultActions addReservationError(Reservation reservation, HttpStatus status, String message)
			throws Exception {
		return checkError(addReservationNoCheck(reservation), status, message);
	}

	private ResultActions addReservationWithOccupationFieldError(Reservation reservation, String... fieldErrors)
			throws Exception {
		ResultActions resultActions = addReservationError(reservation, HttpStatus.BAD_REQUEST,
				"Fehler beim Prüfen der Reservierung");
		int i = 0;
		while (i < fieldErrors.length) {
			assertOccupationFieldError(resultActions, i / 2, fieldErrors[i++], fieldErrors[i++]);
		}
		return resultActions;
	}

	private ResultActions addReservationWithFieldError(Reservation reservation, String... fieldErrors)
			throws Exception {
		ResultActions resultActions = addReservationError(reservation, HttpStatus.BAD_REQUEST,
				"Fehler beim Prüfen der Reservierung");
		int i = 0;
		while (i < fieldErrors.length) {
			assertFieldError(resultActions, i / 2, "reservation", fieldErrors[i++], fieldErrors[i++]);
		}
		return resultActions;
	}

	private ResultActions assertOccupationFieldError(ResultActions resultActions, int i, String field, String message)
			throws Exception {
		return assertFieldError(resultActions, i, "occupation[0]", field, message);
	}

	private ResultActions assertFieldError(ResultActions resultActions, int i, String entity, String field,
			String message) throws Exception {
		return resultActions.andExpect(jsonPath("$.fieldErrors[" + i + "].entity").value(entity))
				.andExpect(jsonPath("$.fieldErrors[" + i + "].field").value(field))
				.andExpect(jsonPath("$.fieldErrors[" + i + "].message").value(message));
	}

	private ResultActions addReservationOverlap(Reservation reservation, int nrOccupation) throws Exception {
		return addReservationWithOccupationFieldError(reservation, "occupation",
				String.format("Reservierung am %tF %tR nicht möglich, weil Platz %s belegt ist.", reservation.getDate(),
						reservation.getStart(), reservation.getCourts()));
	}

	private ResultActions checkReservation(ResultActions resultActions, Reservation reservation, ActionType actionType)
			throws Exception {
		switch (actionType) {
		case CREATE:
			resultActions.andExpect(status().isCreated()) //
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andExpect(jsonPath("$.type").value(reservation.getType().ordinal()));
			Reservation createdReservation = getResponseJson(resultActions, Reservation.class);
			resultActions.andExpect(jsonPath("$.id").value(createdReservation.getId()));
			checkProtocol(ReservationMapper.map(createdReservation), actionType);
			break;
		case MODIFY:
			resultActions.andExpect(status().isOk());
			checkProtocol(ReservationMapper.map(reservation), actionType);
			break;
		case DELETE:
			resultActions.andExpect(status().isOk());
			checkProtocol(ReservationMapper.map(reservation), actionType);
			break;
		}
		return resultActions;
	}

	private ResultActions checkOccupation(ResultActions resultActions, Occupation occupation, ActionType actionType)
			throws Exception {
		switch (actionType) {
		case CREATE:
			resultActions.andExpect(status().isCreated()) //
					.andExpect(jsonPath("$.id").isNotEmpty())
					.andExpect(jsonPath("$.type").value(occupation.getType().ordinal()));
			break;
		case MODIFY:
			resultActions.andExpect(status().isOk());
			checkProtocol(OccupationMapper.map(occupation), actionType);
		case DELETE:
			resultActions.andExpect(status().isOk());
			checkProtocol(OccupationMapper.map(occupation), actionType);
		default:
			break;
		}
		return resultActions;
	}

	private ResultActions checkOccupations(ResultActions resultActions, List<Occupation> occupations) throws Exception {
		if (occupations == null || occupations.isEmpty()) {
			resultActions.andExpect(jsonPath("$").isEmpty());
		} else {
			resultActions.andExpect(jsonPath("$", Matchers.hasSize(occupations.size())));
		}
		return resultActions;

	}

	private ResultActions checkError(ResultActions resultActions, HttpStatus status, String message) throws Exception {
		return resultActions //
				.andExpect(status().is(status.value())) //
				.andExpect(jsonPath("$.message").value(message)) //
		;
	}

	private Reservation getReservation(ResultActions resultAction) throws Exception {
		String content = resultAction.andReturn().getResponse().getContentAsString();
		return objectMapper.readValue(content, Reservation.class);
	}

	private UserEntity createUser(UserRole role, ActivationStatus status) {
		String name = role.toString() + "." + status.toString();
		return new UserEntity(name + "@myDomain.de", name, "top secret", role, status);
	}

	private UserEntity insertUser(UserRole role, ActivationStatus status) {
		return userRepository.save(createUser(role, status));
	}

	private Reservation createReservation(long systemId, int court, int hour, int duration) {
		return createReservation(systemId, null, court, hour, duration);
	}

	private Reservation createReservation(long systemId, UserEntity user, int court, int hour, int duration) {
		return new Reservation(systemId, UserMapper.map(user), "reservation name", String.valueOf(court),
				LocalDate.now().plusDays(1), LocalTime.of(hour, 0), duration, ReservationType.INDIVIDUAL);
	}
}
