package de.tigges.tchreservation.reservation;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

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

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.reservation.model.Reservation;
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

	private User user;
	private User admin;

	@Before
	public void setup() throws Exception {

		this.protocolRepository.deleteAll();
		this.occupationRepository.deleteAll();
		this.reservationRepository.deleteAll();
		this.userRepository.deleteAll();
		user = addUser(UserRole.REGISTERED, ActivationStatus.ACTIVE);
		admin = addUser(UserRole.ADMIN, ActivationStatus.ACTIVE);
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
	public void addReservationNoText() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setText(null);
		addReservationError(reservation, HttpStatus.BAD_REQUEST, "Fehler beim Prüfen der Reservierung");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addReservationInvalidSystemConfig() throws Exception {
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
		assertFieldError(
				addReservationError(createReservation(1, user, 1, 8, 4), HttpStatus.BAD_REQUEST,
						"Fehler beim Prüfen der Reservierung"),
				"duration", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung der Dauer 4 anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypePrepaid() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.PREPAID);
		assertFieldError(
				addReservationError(reservation, HttpStatus.BAD_REQUEST, "Fehler beim Prüfen der Reservierung"), //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ PREPAID anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTournament() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TOURNAMENT);
		assertFieldError(
				addReservationError(reservation, HttpStatus.BAD_REQUEST, "Fehler beim Prüfen der Reservierung"), //
				"type",
				"Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TOURNAMENT anzulegen.");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addReservationUnauthorizedTypeTrainer() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TRAINER);
		assertFieldError(
				addReservationError(reservation, HttpStatus.BAD_REQUEST, "Fehler beim Prüfen der Reservierung"), //
				"type", "Der Benutzer REGISTERED hat nicht die Rechte, eine Reservierung vom Typ TRAINER anzulegen.");
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateReservation() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 12, 2);
		addReservation(reservation);
		reservation.setDuration(6);
		updateReservation(reservation);
	}

	private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
		return performPost("/reservation/add", reservation);
	}

	private ResultActions updateReservation(Reservation reservation) throws Exception {
		return performPut("/reservation/update", reservation);
	}

	private ResultActions addReservation(Reservation reservation) throws Exception {
		return checkReservation(addReservationNoCheck(reservation), reservation, ActionType.CREATE);
	}

	private ResultActions addReservationError(Reservation reservation, HttpStatus status, String message)
			throws Exception {
		return checkError(addReservationNoCheck(reservation), status, message);
	}

	private ResultActions assertFieldError(ResultActions resultActions, String field, String message) throws Exception {
		return resultActions.andExpect(jsonPath("$.fieldErrors[0].entity").value("reservation"))
				.andExpect(jsonPath("$.fieldErrors[0].field").value(field))
				.andExpect(jsonPath("$.fieldErrors[0].message").value(message));
	}

	private ResultActions addReservationOverlap(Reservation reservation) throws Exception {
		return addReservationError(reservation, HttpStatus.BAD_REQUEST,
				String.format("Reservierung am %tF %tR nicht möglich, weil Platz %s belegt ist.", reservation.getDate(),
						reservation.getStart(), reservation.getCourts()));
	}

	private ResultActions checkReservation(ResultActions resultActions, Reservation reservation, ActionType actionType)
			throws Exception {
		if (ActionType.CREATE.equals(actionType)) {
			resultActions.andExpect(status().is(HttpStatus.CREATED.value()));
			Reservation createdReservation = jsonObject(resultActions.andReturn().getResponse().getContentAsByteArray(),
					Reservation.class);
			resultActions.andExpect(jsonPath("$.id").value(createdReservation.getId()));
			checkProtocol(createdReservation, actionType);
		} else if (ActionType.MODIFY.equals(actionType) || ActionType.DELETE.equals(actionType)) {
			checkProtocol(reservation, actionType);
		}
		return resultActions //
				.andExpect(status().isCreated()) //
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.type").value(reservation.getType().ordinal()))
		//
		;
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
