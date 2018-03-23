package de.tigges.tchreservation.reservation;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
	public void addReservationOtherSystem() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system2, admin, 1, 10, 2));
	}

	@Test
	public void addReservationOtherCourt() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system1, user, 2, 10, 2));
	}
	
	@Test
	public void addReservationOtherTime() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 2));
		addReservation(createReservation(system1, user, 1, 11, 2));
	}

	@Test
	public void addReservationNotAvailable() throws Exception {
		addReservation(createReservation(system1, user, 1, 10, 3));
		addReservation(createReservation(system1, user, 1, 11, 2));
	}

	private void addReservation(Reservation reservation) throws Exception {
		checkReservation(
				mockMvc.perform(post("/reservation/add").content(this.json(reservation)).contentType(contentType))
						.andExpect(status().isOk()),
				reservation);
	}

	private User createUser(UserRole role, ActivationStatus status) {
		String name = role.toString() + "." + status.toString();
		return new User(name + "@myDomain.de", name, "top secret", role, status);
	}

	private Reservation createReservation(ReservationSystemConfig system, User user, int court, int hour,
			int duration) {
		return new Reservation(system, user, "reservation name", court,
				LocalDateTime.now().plusDays(1).withHour(hour).withMinute(0).withSecond(0), duration,
				ReservationType.INDIVIDUAL);
	}

	private ResultActions checkReservation(ResultActions resultActions, Reservation reservation) throws Exception {
		resultActions.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.type").value(reservation.getType().toString()))
		//
		;
		return resultActions;
	}

	// @Test
	// public void userNotFound() throws Exception {
	// mockMvc.perform(
	// post("/george/bookmarks/").content(this.json(new Bookmark(null, null,
	// null))).contentType(contentType))
	// .andExpect(status().isNotFound());
	// }

	// @Test
	// public void readSingleBookmark() throws Exception {
	// mockMvc.perform(get("/" + userName + "/bookmarks/" +
	// this.bookmarkList.get(0).getId()))
	// .andExpect(status().isOk()).andExpect(content().contentType(contentType))
	// .andExpect(jsonPath("$.id", is(this.bookmarkList.get(0).getId().intValue())))
	// .andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + userName)))
	// .andExpect(jsonPath("$.description", is("A description")));
	// }

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
