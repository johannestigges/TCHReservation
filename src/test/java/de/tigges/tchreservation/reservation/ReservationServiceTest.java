package de.tigges.tchreservation.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserRepository;
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
	private UserRepository userRepository;
	@Autowired
	private ProtocolRepository protocolRepository;

	private User user;
	private User admin;

	@Before
	public void setup() throws Exception {

		this.protocolRepository.deleteAll();
		this.occupationRepository.deleteAll();
		this.reservationRepository.deleteAll();
		this.userRepository.deleteAll();

		user = insertUser(UserRole.REGISTERED, ActivationStatus.ACTIVE);
		admin = insertUser(UserRole.ADMIN, ActivationStatus.ACTIVE);
	}

	@Test
	public void addReservation() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
	}

	@Test
	public void addReservationSystems() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(2, admin, 1, 10, 2));
	}

	@Test
	public void addReservationCourts() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(1, user, 2, 10, 2));
	}

	@Test
	public void addReservationTimes() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 2));
		addReservation(createReservation(1, user, 1, 11, 2));
	}

	@Test
	public void addReservationOverlap() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 3));
		addReservationOverlap(createReservation(1, user, 1, 11, 2));
	}

	@Test
	public void addReservationDuplicate() throws Exception {
		addReservation(createReservation(1, user, 1, 10, 3));
		addReservationOverlap(createReservation(1, user, 1, 10, 3));
	}

	@Test
	public void addReservationOverlap2() throws Exception {
		addReservation(createReservation(1, admin, 1, 10, 6));
		addReservationOverlap(createReservation(1, user, 1, 11, 2));
	}

	@Test
	public void addReservationOverlap3() throws Exception {
		addReservation(createReservation(1, user, 1, 11, 2));
		addReservationOverlap(createReservation(1, admin, 1, 10, 6));
	}

	@Test
	public void addReservationNoText() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setText(null);
		addReservationError(reservation, HttpStatus.BAD_REQUEST, "error validation reservation");
	}

	@Test
	public void addReservationInvalidSystemConfig() throws Exception {
		addReservationError(createReservation(0, admin, 1, 10, 6), HttpStatus.BAD_REQUEST, "no reservation system");
	}

	@Test
	public void addReservationUnknownSystemConfig() throws Exception {
		addReservationError(createReservation(10, admin, 1, 10, 6), HttpStatus.NOT_FOUND,
				"SYSTEM_CONFIGURATION with id 10 not found");
	}

	@Test
	public void addReservationNoUser() throws Exception {
		addReservationError(createReservation(1, null, 1, 8, 2), HttpStatus.BAD_REQUEST, "no user");
	}

	@Test
	public void addReservationInvalidUser() throws Exception {
		User user = new User();
		user.setId(0);
		addReservationError(createReservation(1, user, 1, 8, 2), HttpStatus.BAD_REQUEST, "no user");
	}

	@Test
	public void addReservationUnknownUser() throws Exception {
		User user = new User();
		user.setId(100);
		addReservationError(createReservation(1, user, 1, 8, 2), HttpStatus.NOT_FOUND, "USER with id 100 not found");
	}

	@Test
	public void addReservationUserInvalidStatusCreated() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.CREATED is not active.");
	}

	@Test
	public void addReservationUserInvalidStatusLocked() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.LOCKED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.LOCKED is not active.");
	}

	@Test
	public void addReservationUserInvalidStatusRemoved() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.REMOVED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.REMOVED is not active.");
	}

	@Test
	public void addReservationUserInvalidStatusVerified() throws Exception {
		addReservationError(
				createReservation(1, insertUser(UserRole.REGISTERED, ActivationStatus.VERIFIED_BY_USER), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.VERIFIED_BY_USER is not active.");
	}

	@Test
	public void addReservationAnonymousUser() throws Exception {
		addReservationError(createReservation(1, insertUser(UserRole.ANONYMOUS, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user with role ANONYMOUS cannot add reservation.");
	}

	@Test
	public void addReservationUnauthorizedDuration() throws Exception {
		addReservationError(createReservation(1, user, 1, 8, 4), HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation with duration 4.");
	}

	@Test
	public void addReservationUnauthorizedTypePrepaid() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.PREPAID);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type PREPAID.");
	}

	@Test
	public void addReservationUnauthorizedTypeTournament() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TOURNAMENT);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type TOURNAMENT.");
	}

	@Test
	public void addReservationUnauthorizedTypeTrainer() throws Exception {
		Reservation reservation = createReservation(1, user, 1, 8, 2);
		reservation.setType(ReservationType.TRAINER);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type TRAINER.");
	}

	private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
		return mockMvc.perform(post("/reservation/add").content(this.json(reservation)).contentType(contentType));
	}

	private ResultActions addReservation(Reservation reservation) throws Exception {
		return checkReservation(addReservationNoCheck(reservation), reservation, ActionType.CREATE);
	}

	private ResultActions addReservationError(Reservation reservation, HttpStatus status, String message)
			throws Exception {
		return checkError(addReservationNoCheck(reservation), status, message);
	}

	private ResultActions addReservationOverlap(Reservation reservation) throws Exception {
		return addReservationError(reservation, HttpStatus.BAD_REQUEST,
				String.format("cannot add occupation on %tF %tR becauce court %s ist occupied.", reservation.getDate(),
						reservation.getStart(), reservation.getCourts()));
	}

	private ResultActions checkReservation(ResultActions resultActions, Reservation reservation, ActionType actionType)
			throws Exception {
		if (ActionType.CREATE.equals(actionType)) {
			Reservation createdReservation = jsonObject(resultActions.andReturn().getResponse().getContentAsByteArray(), Reservation.class);
			resultActions.andExpect(jsonPath("$.id").value(createdReservation.getId()));
			checkProtocol(createdReservation, actionType);
		} else if (ActionType.MODIFY.equals(actionType) || ActionType.DELETE.equals(actionType)) {
			checkProtocol(reservation, actionType);
		}
		return resultActions //
				.andExpect(status().isCreated()) //
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.type").value(reservation.getType().toString()))
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
