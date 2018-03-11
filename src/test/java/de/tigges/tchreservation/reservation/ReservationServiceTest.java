package de.tigges.tchreservation.reservation;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
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
import org.springframework.web.context.WebApplicationContext;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.reservation.model.OccupationRepository;
import de.tigges.tchreservation.reservation.model.OccupationType;
import de.tigges.tchreservation.reservation.model.Reservation;
import de.tigges.tchreservation.reservation.model.ReservationRepository;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfigEntity;
import de.tigges.tchreservation.reservation.model.ReservationSystemRespoitory;
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

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private ReservationSystemRespoitory reservationSystemRepository;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private ReservationSystemConfigEntity system1;

	private ReservationSystemConfigEntity system2;

	private User admin;

	private User user;

	private User waitingUser;

	private User lockedUser;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.reservationRepository.deleteAll();
		this.occupationRepository.deleteAll();
		this.reservationSystemRepository.deleteAll();
		this.userRepository.deleteAll();

		system1 = this.reservationSystemRepository
				.save(new ReservationSystemConfigEntity(1, "Ascheplätze", 6, 30, 8, 22));
		system2 = this.reservationSystemRepository
				.save(new ReservationSystemConfigEntity(2, "Hallenplätze", 2, 60, 8, 22));

		admin = this.userRepository
				.save(new User("admin@myDomain.de", "admin", "geheim", UserRole.ADMIN, ActivationStatus.ACTIVE));
		user = this.userRepository
				.save(new User("user@myDomain.de", "user", "geheim", UserRole.REGISTERED, ActivationStatus.ACTIVE));
		waitingUser = this.userRepository.save(
				new User("waiting@myDomain.de", "waiting", "geheim", UserRole.REGISTERED, ActivationStatus.CREATED));
		lockedUser = this.userRepository
				.save(new User("locked@myDomain.de", "locked", "geheim", UserRole.REGISTERED, ActivationStatus.LOCKED));
	}

	@Test
	public void addReservation() throws Exception {
		mockMvc.perform(post("/addReservation") //
				.content(this.json(new Reservation(system1.getId(), "reservation name", user.getId(), 1, null, 2,
						OccupationType.INDIVIDUAL)))
				.contentType(contentType)).andExpect(status().is2xxSuccessful());
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
