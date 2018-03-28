package de.tigges.tchreservation.reservation;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class ReservationServiceTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	private HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private ReservationSystemConfigRepository reservationSystemConfigRepository;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private ReservationSystemConfig system1;

	private ReservationSystemConfig system2;

	private User admin;

	private User user;

	private User waitingUser;

	private User lockedUser;

	@SuppressWarnings("unchecked")
	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = (HttpMessageConverter<Object>) Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.occupationRepository.deleteAll();
		this.reservationRepository.deleteAll();
		this.reservationSystemConfigRepository.deleteAll();
		this.userRepository.deleteAll();

		system1 = this.reservationSystemConfigRepository.save(new ReservationSystemConfig("Ascheplätze", 6, 30, 8, 22));
		system2 = this.reservationSystemConfigRepository
				.save(new ReservationSystemConfig("Hallenplätze", 2, 60, 8, 22));

		admin = this.userRepository.save(createUser(UserRole.ADMIN, ActivationStatus.ACTIVE));
		user = this.userRepository.save(createUser(UserRole.REGISTERED, ActivationStatus.ACTIVE));
		waitingUser = this.userRepository.save(createUser(UserRole.REGISTERED, ActivationStatus.CREATED));
		lockedUser = this.userRepository.save(createUser(UserRole.REGISTERED, ActivationStatus.LOCKED));

	}

	@Test
	public void addReservation() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
	}

	@Test
	public void addReservationSystems() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system2, admin, 1, 10, 2));
	}

	@Test
	public void addReservationCourts() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system1, user, 2, 10, 2));
	}

	@Test
	public void addReservationTimes() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system1, user, 1, 11, 2));
	}

	@Test
	public void addReservationOverlap() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 3));
		addReservationError(createReservation(system1, user, 1, 11, 2), HttpStatus.BAD_REQUEST, "overlap!");
	}

	@Test
	public void addReservationDuplicate() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 3));
		addReservationError(createReservation(system1, user, 1, 10, 3), HttpStatus.BAD_REQUEST, "overlap!");
	}

	@Test
	public void addReservationOverlap2() throws Exception {
		addReservation(createReservation(system1, admin, 1, 10, 6));
		addReservationError(createReservation(system1, user, 1, 11, 2), HttpStatus.BAD_REQUEST, "overlap!");
	}

	@Test
	public void addReservationOverlap3() throws Exception {
		addReservation(createReservation(system1, user, 1, 11, 2));
		addReservationError(createReservation(system1, admin, 1, 10, 6), HttpStatus.BAD_REQUEST, "overlap!");
	}

	@Test
	public void addReservationNoText() throws Exception {
		Reservation reservation = createReservation(system1, user, 1, 8, 2);
		reservation.setText(null);
		addReservationError(reservation, HttpStatus.BAD_REQUEST, "error validation reservation");
	}

	@Test
	public void addReservationNoSystemConfig() throws Exception {
		addReservationError(createReservation(null, admin, 1, 10, 6), HttpStatus.BAD_REQUEST, "no reservation system");
	}

	@Test
	public void addReservationInvalidSystemConfig() throws Exception {
		ReservationSystemConfig config = new ReservationSystemConfig("invalid", 1, 60, 8, 20);
		config.setId(0);
		addReservationError(createReservation(config, admin, 1, 10, 6), HttpStatus.BAD_REQUEST,
				"no reservation system");
	}

	@Test
	public void addReservationUnknownSystemConfig() throws Exception {
		ReservationSystemConfig config = new ReservationSystemConfig("invalid", 1, 60, 8, 20);
		config.setId(10);
		addReservationError(createReservation(config, admin, 1, 10, 6), HttpStatus.NOT_FOUND,
				"System config with id 10 not found");
	}

	@Test
	public void addReservationNoUser() throws Exception {
		addReservationError(createReservation(system1, null, 1, 8, 2), HttpStatus.BAD_REQUEST, "no user");
	}

	@Test
	public void addReservationInvalidUser() throws Exception {
		User user = new User();
		user.setId(0);
		addReservationError(createReservation(system1, user, 1, 8, 2), HttpStatus.BAD_REQUEST, "no user");
	}

	@Test
	public void addReservationUnknownUser() throws Exception {
		User user = new User();
		user.setId(100);
		addReservationError(createReservation(system1, user, 1, 8, 2), HttpStatus.NOT_FOUND,
				"user with id 100 not found");
	}

	@Test
	public void addReservationUserInvalidStatusCreated() throws Exception {
		addReservationError(
				createReservation(system1, insertUser(UserRole.REGISTERED, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.CREATED is not active.");
	}
	@Test
	public void addReservationUserInvalidStatusLocked() throws Exception {
		addReservationError(
				createReservation(system1, insertUser(UserRole.REGISTERED, ActivationStatus.LOCKED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.LOCKED is not active.");
	}
	@Test
	public void addReservationUserInvalidStatusRemoved() throws Exception {
		addReservationError(
				createReservation(system1, insertUser(UserRole.REGISTERED, ActivationStatus.REMOVED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.REMOVED is not active.");
	}
	@Test
	public void addReservationUserInvalidStatusVerified() throws Exception {
		addReservationError(
				createReservation(system1, insertUser(UserRole.REGISTERED, ActivationStatus.VERIFIED_BY_USER), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user REGISTERED.VERIFIED_BY_USER is not active.");
	}

	@Test
	public void addReservationAnonymousUser() throws Exception {
		addReservationError(
				createReservation(system1, insertUser(UserRole.ANONYMOUS, ActivationStatus.CREATED), 1, 8, 2),
				HttpStatus.UNAUTHORIZED, "user with role ANONYMOUS cannot add reservation.");
	}

	@Test
	public void addReservationUnauthorizedDuration() throws Exception {
		addReservationError(createReservation(system1, user, 1, 8, 4), HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation with duration 4.");
	}

	@Test
	public void addReservationUnauthorizedTypePrepaid() throws Exception {
		Reservation reservation = createReservation(system1, user, 1, 8, 2);
		reservation.setType(ReservationType.PREPAID);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type PREPAID.");
	}

	@Test
	public void addReservationUnauthorizedTypeTournament() throws Exception {
		Reservation reservation = createReservation(system1, user, 1, 8, 2);
		reservation.setType(ReservationType.TOURNAMENT);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type TOURNAMENT.");
	}

	@Test
	public void addReservationUnauthorizedTypeTrainer() throws Exception {
		Reservation reservation = createReservation(system1, user, 1, 8, 2);
		reservation.setType(ReservationType.TRAINER);
		addReservationError(reservation, HttpStatus.UNAUTHORIZED,
				"user REGISTERED.ACTIVE with role REGISTERED cannot add reservation of type TRAINER.");
	}

	private ResultActions addReservationNoCheck(Reservation reservation) throws Exception {
		return mockMvc.perform(post("/reservation/add").content(this.json(reservation)).contentType(contentType));
	}

	private ResultActions addReservation(Reservation reservation) throws Exception {
		return checkReservation(addReservationNoCheck(reservation), reservation);
	}

	private ResultActions addReservationError(Reservation reservation, HttpStatus status, String message)
			throws Exception {
		return checkError(addReservationNoCheck(reservation), status, message);
	}

	private ResultActions checkReservation(ResultActions resultActions, Reservation reservation) throws Exception {
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

	private Reservation createReservation(ReservationSystemConfig system, User user, int court, int hour,
			int duration) {
		return new Reservation(system, user, "reservation name", court, LocalDate.now().plusDays(1),
				LocalTime.of(hour, 0), duration, ReservationType.INDIVIDUAL);
	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
