package de.tigges.tchreservation.systemconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
class SystemConfigServiceTest extends ProtocolTest {

	@Autowired
	private SystemConfigRepository systemConfigRepository;

	@Autowired
	ObjectMapper objectMapper;

	private UserEntity user;
	private UserEntity admin;

	@BeforeEach
	public void setup() throws Exception {
		protocolRepository.deleteAll();
		userRepository.deleteAll();
		systemConfigRepository.deleteAll();
		user = addUser(UserRole.REGISTERED);
		admin = addUser(UserRole.ADMIN);
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	void getOne() throws Exception {
		SystemConfigEntity entity = createSystemConfig(1L, "Platz 1");
		verifyEquals(get(1L), SystemConfigMapper.map(entity));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	void testGetAll() throws Exception {
		createSystemConfig(1L, "Platz 1");
		createSystemConfig(2L, "Center Court,Roland Garros,Nebenplatz");
		assertThat(getAll().andReturn().getResponse().getContentAsString()).isEqualTo(
				"[{\"id\":1,\"name\":\"unit test 1\",\"courts\":[\"Platz 1\"],\"durationUnitInMinutes\":30,\"maxDaysReservationInFuture\":2,\"maxDuration\":3,\"openingHour\":8,\"closingHour\":22},{\"id\":2,\"name\":\"unit test 2\",\"courts\":[\"Center Court,Roland Garros,Nebenplatz\"],\"durationUnitInMinutes\":30,\"maxDaysReservationInFuture\":2,\"maxDuration\":3,\"openingHour\":8,\"closingHour\":22}]");
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	void testGetAllNotAllowed() throws Exception {
		performGet("/systemconfig").andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	void testAddNotAllowed() throws Exception {
		performPost("/systemconfig", new ReservationSystemConfig()).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	void testUpdateNotAllowed() throws Exception {
		performPut("/systemconfig", new ReservationSystemConfig()).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	void testAdd() throws Exception {
		ReservationSystemConfig config = new ReservationSystemConfig(1, "unit test 1",
				Arrays.asList("Platz 1", "Platz 2", "Platz 3", "Platz 4", "Platz 5", "Platz 6"), 30, 2, 3, 8, 22);
		performPost("/systemconfig", config).andExpect(status().isCreated());
		verify(config);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	void testUpdate() throws Exception {
		createSystemConfig(1L, "Platz 1");
		ReservationSystemConfig config = get(1L);
		config.getCourts().add("Platz 2");
		config.getCourts().add("Platz 3");
		config.getCourts().add("Platz 4");
		config.getCourts().add("Platz 5");
		config.getCourts().add("Platz 6");
		config.setName("Toller neuer Name!");
		config.setMaxDaysReservationInFuture(7);
		config.setMaxDuration(10);
		config.setOpeningHour(10);
		config.setClosingHour(20);
		performPut("/systemconfig", config).andExpect(status().is2xxSuccessful());
		verifyEquals(get(1L), config);
	}

	private ReservationSystemConfig get(long id) throws Exception {
		String content = performGet("/systemconfig/" + String.valueOf(id)).andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readValue(content, ReservationSystemConfig.class);
	}

	private ResultActions getAll() throws Exception {
		return performGet("/systemconfig").andExpect(status().is2xxSuccessful());
	}

	private void verify(ReservationSystemConfig config) throws Exception {
		verifyEquals(config, get(config.getId()));
	}

	private void verifyEquals(ReservationSystemConfig c1, ReservationSystemConfig c2) {
		assertThat(c1.getId()).isEqualTo(c2.getId());
		assertThat(c1.getName()).isEqualTo(c2.getName());
		assertThat(c1.getCourts()).hasSameElementsAs(c2.getCourts());
		assertThat(c1.getDurationUnitInMinutes()).isEqualTo(c2.getDurationUnitInMinutes());
		assertThat(c1.getMaxDaysReservationInFuture()).isEqualTo(c2.getMaxDaysReservationInFuture());
		assertThat(c1.getMaxDuration()).isEqualTo(c2.getMaxDuration());
		assertThat(c1.getOpeningHour()).isEqualTo(c1.getOpeningHour());
		assertThat(c1.getClosingHour()).isEqualTo(c2.getClosingHour());
	}

	private SystemConfigEntity createSystemConfig(long id, String courts) {
		SystemConfigEntity e = new SystemConfigEntity();
		e.setId(id);
		e.setName("unit test " + id);
		e.setCourts(courts);
		e.setDurationUnitInMinutes(30);
		e.setMaxDaysReservationInFuture(2);
		e.setMaxDuration(3);
		e.setOpeningHour(8);
		e.setClosingHour(22);
		systemConfigRepository.save(e);
		return e;
	}
}
