package de.tigges.tchreservation.systemconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeEntity;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
class SystemConfigServiceTest extends ProtocolTest {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    private UserEntity user;
    private UserEntity admin;
    @Autowired
    private ReservationTypeRepository reservationTypeRepository;

    @BeforeEach
    public void setup() {
        protocolRepository.deleteAll();
        userRepository.deleteAll();
        reservationTypeRepository.deleteAll();
        systemConfigRepository.deleteAll();
        user = addUser(UserRole.REGISTERED);
        admin = addUser(UserRole.ADMIN);
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void getOne() throws Exception {
        SystemConfigEntity entity = createSystemConfigEntity(1L, "Platz 1");
        verifyEquals(get(1L), SystemConfigMapper.map(entity));
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void testGetAll() throws Exception {
        // @formatter:off
		List<ReservationSystemConfig> configs = Arrays.asList(
				SystemConfigMapper.map(createSystemConfigEntity(1L, "Platz 1")),
				SystemConfigMapper.map(createSystemConfigEntity(2L, "Center Court,Roland Garros,Nebenplatz"))
				);
		// @formatter:on
        var response = responseAll(getAll().andExpect(status().is2xxSuccessful()));
        assertThat(response).containsAll(configs);
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void testAddNotAllowed() throws Exception {
        performPost("/rest/systemconfig", SystemConfigMapper.map(createSystemConfigEntity(1, ""))).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void testUpdateNotAllowed() throws Exception {
        performPut("/rest/systemconfig", SystemConfigMapper.map(createSystemConfigEntity(1, ""))).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    void testDeleteNotAllowed() throws Exception {
        performDelete("/rest/systemconfig/1").andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ADMIN")
    void testAdd() throws Exception {
        ReservationSystemConfig config = new ReservationSystemConfig(1, "unit test 1", "title unit test 1",
                Arrays.asList("Platz 1", "Platz 2", "Platz 3", "Platz 4", "Platz 5", "Platz 6"),
                30, 2, 3, 8, 22,
                List.of(
                        new SystemConfigReservationType(1L, 1, "Quickbuchung", 3, 1, 0,
                                Arrays.asList(UserRole.REGISTERED, UserRole.KIOSK, UserRole.TECHNICAL, UserRole.TEAMSTER, UserRole.TRAINER)),
                        new SystemConfigReservationType(2L, 2, "Training", 0, 0, 0,
                                Arrays.asList(UserRole.TEAMSTER, UserRole.TRAINER)),
                        new SystemConfigReservationType(3L, 3, "Meisterschaft", 0, 0, 0,
                                Arrays.asList(UserRole.TEAMSTER, UserRole.TRAINER)),
                        new SystemConfigReservationType(3L, 3, "Dauerbuchung", 0, 0, 0,
                                Arrays.asList(UserRole.TRAINER))
                ));
        verifyEquals(config, performPost("/rest/systemconfig", config).andExpect(status().isCreated()));
        verifyEquals(config, get(1L));
    }

    @Test
    @WithMockUser(username = "ADMIN")
    void testUpdate() throws Exception {
        createSystemConfigEntity(1L, "Platz 1");
        var c = get(1L);
        var config = new ReservationSystemConfig(c.id(), "new name", "new title", Arrays.asList(c.courts().get(0), "Platz 2", "Platz 3"), 30, 7, 10, 10, 20, c.types());

        verifyEquals(config, performPut("/rest/systemconfig", config).andExpect(status().is2xxSuccessful()));
        verifyEquals(config, get(1L));
    }

    @Test
    @WithMockUser(username = "ADMIN")
    void testDelete() throws Exception {
        ReservationSystemConfig config1 = SystemConfigMapper.map(createSystemConfigEntity(1L, "Platz 1"));
        ReservationSystemConfig config2 = SystemConfigMapper
                .map(createSystemConfigEntity(2L, "Center Court,Roland Garros,Nebenplatz"));

        verifyEquals(config2, performDelete("/rest/systemconfig/2").andExpect(status().is2xxSuccessful()));
        List<ReservationSystemConfig> configs = responseAll(getAll().andExpect(status().is2xxSuccessful()));
        assertThat(configs).hasSize(1);
        ReservationSystemConfig config3 = configs.get(0);
        verifyEquals(config1, config3);
    }

    private ReservationSystemConfig get(long id) throws Exception {
        return response(
                performGet("/rest/systemconfig/getone/" + id).andExpect(status().is2xxSuccessful()));
    }

    private ResultActions getAll() throws Exception {
        return performGet("/rest/systemconfig/getall").andExpect(status().is2xxSuccessful());
    }

    private ReservationSystemConfig response(ResultActions resultActions) throws Exception {
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readValue(contentAsString, ReservationSystemConfig.class);
    }

    private List<ReservationSystemConfig> responseAll(ResultActions resultActions) throws Exception {
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        return Arrays.asList(new ObjectMapper().readValue(contentAsString, ReservationSystemConfig[].class));
    }

    private void verifyEquals(ReservationSystemConfig config, ResultActions resultActions) throws Exception {
        verifyEquals(config, response(resultActions));
    }

    private void verifyEquals(ReservationSystemConfig c1, ReservationSystemConfig c2) {
        assertThat(c1.id()).isEqualTo(c2.id());
        assertThat(c1.name()).isEqualTo(c2.name());
        if (c1.title() == null) {
            assertThat(c2.title()).isNull();
        } else {
            assertThat(c1.title()).isEqualTo(c2.title());
        }
        assertThat(c1.courts()).hasSameElementsAs(c2.courts());
        assertThat(c1.durationUnitInMinutes()).isEqualTo(c2.durationUnitInMinutes());
        assertThat(c1.maxDaysReservationInFuture()).isEqualTo(c2.maxDaysReservationInFuture());
        assertThat(c1.maxDuration()).isEqualTo(c2.maxDuration());
        assertThat(c1.openingHour()).isEqualTo(c2.openingHour());
        assertThat(c1.closingHour()).isEqualTo(c2.closingHour());
    }

    private SystemConfigEntity createSystemConfigEntity(long id, String courts) {
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

        ReservationTypeEntity type = new ReservationTypeEntity();
        type.setSystemConfig(e);
        type.setType(1);
        type.setName("Quichbuchungen");
        type.setMaxDuration(3);
        type.setMaxDaysReservationInFuture(1);
        type.setMaxCancelInHours(0);
        type.setRoles(String.join(",", UserRole.REGISTERED.name(), UserRole.TRAINER.name()));
        reservationTypeRepository.save(type);

        e.setTypes(Set.of(type));
        return e;
    }
}
