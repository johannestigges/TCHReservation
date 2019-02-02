package de.tigges.tchreservation.reservation;

import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class ReservationServiceTest extends ProtocolTest {

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private OccupationRepository occupationRepository;

	@Autowired
	ObjectMapper objectMapper;

	private User user;
	private User admin;

	@Before
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
		addReservation(createReservation(1, user, 1, 10, 2));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationSystems() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(2, admin, 1, 10, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourts() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(1, user, 2, 10, 2));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationTimes() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(1, user, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 3));
		addReservationOverlap(createReservation(1, user, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDuplicate() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 3));
		addReservationOverlap(createReservation(1, user, 1, 10, 3));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap2() throws Exception {
		addReservation(createReservation(1, admin, 1, 10, 6));
		addReservationOverlap(createReservation(1, user, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationOverlap3() throws Exception {
		addReservation(createReservation(1, user, 1, 11, 2));
		addReservationOverlap(createReservation(1, admin, 1, 10, 6));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNotOverlapDifferentSystemConfig() throws Exception {
		addReservation(createReservation(1, admin, 1, 11, 2));
		addReservation(createReservation(2, admin, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNotOverlapDifferentDays() throws Exception {
		Reservation reservation = createReservation(1, admin, 1, 11, 2);
		reservation.setDate(reservation.getDate().plusDays(1));
		addReservation(reservation);
		addReservation(createReservation(1, admin, 1, 11, 2));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoText() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setText(null);
		addReservationFieldError(reservation, "text", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoDate() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setDate(null);
		addReservationFieldError(reservation, "date", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoStart() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setStart(null);
		addReservationFieldError(reservation, "start", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationBeforeOpeningHour() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 6, 2);
		addReservationFieldError(reservation, "start", "Startzeit 06:00 liegt vor der Öffnungszeit 08:00.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationAfterClosingHour() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 23, 2);
		addReservationFieldError(reservation, "start", "Startzeit 23:00 liegt später als die Endezeit 22:00");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationInvalidMinutes() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 0, 2);
		reservation.setStart(LocalTime.of(9, 25));
		addReservationFieldError(reservation, "start", "fehlerhafte Startzeit mit 25 Minuten.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationExtendsClosingHour() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 19, 7);
		addReservationFieldError(reservation, "start", "Startzeit + Dauer zu spät.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationDurationTooSmall() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 0);
		addReservationFieldError(reservation, "duration", "Dauer zu gering");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoCourts() throws Exception {
		Reservation reservation = createReservation(1, user, 0, 8, 2);
		reservation.setCourts(null);
		addReservationFieldError(reservation, "court", "Bitte geben Sie einen Wert an");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationTooManyCourts() throws Exception {
		Reservation reservation = createReservation(1, user, 0, 8, 2);
		reservation.setCourts("1 2 3 4 5 6 7");
		addReservationFieldError(reservation, "court", "Mehr als 6 Plätze gibt es nicht.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourtTooSmall() throws Exception {
		Reservation reservation = createReservation(1, user, 0, 8, 2);
		addReservationFieldError(reservation, "court", "Platz[1]: Platz 0 gibt es nicht");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationCourtTooBig() throws Exception {
		Reservation reservation = createReservation(1, user, 7, 8, 2);
		addReservationFieldError(reservation, "court", "Platz[1]: Platz 7 gibt es nicht");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNull() throws Exception {
		addReservationNoCheck(null).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoSystemConfig() throws Exception {
		addReservationError(createReservation(0, admin, 1, 10, 6), HttpStatus.BAD_REQUEST,
				"Kein Reservierungssystem angegeben");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationUnknownSystemConfig() throws Exception {
		addReservationError(createReservation(10, admin, 1, 10, 6), HttpStatus.NOT_FOUND,
				"SYSTEM_CONFIGURATION with id 10 not found");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationNoUser() throws Exception {
		addReservationError(createReservation(1, null, 1, 8, 2), HttpStatus.BAD_REQUEST, "Kein User angegeben");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationInvalidUser() throws Exception {
		User user = new User();
		user.setId(0);
		addReservationError(createReservation(1, user, 1, 8, 2), HttpStatus.BAD_REQUEST, "Kein User angegeben");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationUnknownUser() throws Exception {
		User user = new User();
		user.setId(100);
		addReservationError(createReservation(1, user, 1, 8, 2), HttpStatus.NOT_FOUND, "USER with id 100 not found");
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
	public void addReservationUnauthorizedDuration() throws Exception {
		addReservationFieldError(createReservation(1, user, 1, 8, 4), //
				"duration", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung der Dauer 4 anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedDateInThePast() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 10, 2);
		reservation.setDate(LocalDate.now().minusDays(1));
		addReservationFieldError(reservation, "date", "Das Datum darf nicht in der Vergangenheit liegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTimeInThePast() throws Exception {
		int hour = LocalTime.now().getHour();
		ReservationSystemConfig systemConfig = getSystemConfig(1);
		if (hour > systemConfig.getOpeningHour() + 1 && hour < systemConfig.getClosingHour()) {
			Reservation reservation = createReservation(1, user, 1, hour - 1, 1);
			reservation.setDate(LocalDate.now());
			addReservationFieldError(reservation, "time", "Die Startzeit darf nicht in der Vergangenheit liegen.");
		}
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypePrepaid() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.PREPAID);

		addReservationFieldError(reservation, //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ PREPAID anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTournament() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TOURNAMENT);
		addReservationFieldError(reservation, //
				"type",
				"Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TOURNAMENT anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTrainer() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TRAINER);
		addReservationFieldError(reservation, //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TRAINER anzulegen.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateReservation() throws Exception {
		Reservation reservation = getResponseJson(addReservation(createReservation(1, user, 1, 12, 2)),
				Reservation.class);
		reservation.setDuration(6);
		checkReservation(updateReservation(reservation), reservation, ActionType.MODIFY);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateReservationNotFound() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 12, 2);
		reservation.setId(657865765659L);
		updateReservation(reservation).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateReservationNull() throws Exception {
		updateReservation(null).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteReservation() throws Exception {
		Reservation reservation = objectMapper.readValue(addReservation(createReservation(1, user, 1, 10, 2))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Reservation.class);
		checkReservation(deleteReservation(reservation.getId()), reservation, ActionType.DELETE);

		assertThat(reservationRepository.findById(reservation.getId()), Matchers.equalTo(Optional.empty()));
		assertThat(occupationRepository.findByReservationId(reservation.getId()), IsEmptyIterable.emptyIterable());
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
	public void getOccupations() throws Exception {
		performGet("/reservation/getOccupations/1/0").andExpect(status().isOk());
	}

	@Test
	public void getOccupationsWithDate() throws Exception {
		performGet("/reservation/getOccupations/15/" + new Date().getTime()).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getReservation() throws Exception {
		Reservation reservation = reservationRepository.save(createReservation(1, user, 1, 10, 2));

		performGet("/reservation/get/" + reservation.getId()).andExpect(status().isOk());
	}

	private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
		return performPost("/reservation/add", reservation);
	}

	private ResultActions updateReservation(Reservation reservation) throws Exception {
		return performPut("/reservation/update", reservation);
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

	private ResultActions addReservationFieldError(Reservation reservation, String... fieldErrors) throws Exception {
		ResultActions resultActions = addReservationError(reservation, HttpStatus.BAD_REQUEST,
				"Fehler beim Prüfen der Reservierung");
		int i = 0;
		while (i < fieldErrors.length) {
			assertFieldError(resultActions, i / 2, fieldErrors[i++], fieldErrors[i++]);
		}
		return resultActions;
	}

	private ResultActions assertFieldError(ResultActions resultActions, int i, String field, String message)
			throws Exception {
		return resultActions.andExpect(jsonPath("$.fieldErrors[" + i + "].entity").value("reservation"))
				.andExpect(jsonPath("$.fieldErrors[" + i + "].field").value(field))
				.andExpect(jsonPath("$.fieldErrors[" + i + "].message").value(message));
	}

	private ResultActions addReservationOverlap(Reservation reservation) throws Exception {
		return addReservationError(reservation, HttpStatus.BAD_REQUEST,
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
			checkProtocol(createdReservation, actionType);
			break;
		case MODIFY:
			resultActions.andExpect(status().isOk());
			checkProtocol(reservation, actionType);
			break;
		case DELETE:
			resultActions.andExpect(status().isOk());
			checkProtocol(reservation, actionType);
			break;
		}
		return resultActions;
	}

	private ResultActions checkError(ResultActions resultActions, HttpStatus status, String message) throws Exception {
		return resultActions //
				.andExpect(status().is(status.value())) //
				.andExpect(jsonPath("$.message").value(message)) //
		;
	}

	private User createUser(UserRole role, ActivationStatus status) {
		String name = role.toString() + "." + status.toString();
		return new User(name + "@myDomain.de", name, "top secret", role, status);
	}

	private User insertUser(UserRole role, ActivationStatus status) {
		return userRepository.save(createUser(role, status));
	}

	private Reservation createReservation(long systemId, User user, int court, int hour, int duration) {
		return new Reservation(systemId, user, "reservation name", String.valueOf(court), LocalDate.now().plusDays(1),
				LocalTime.of(hour, 0), duration, ReservationType.INDIVIDUAL);
	}
}
