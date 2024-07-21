package de.tigges.tchreservation.protocol;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.reservation.jpa.OccupationEntity;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@WebAppConfiguration
public class ProtocolServiceTest extends ProtocolTest {

    private UserEntity user;
    private UserEntity admin;

    @BeforeEach
    public void setup() {
        this.protocolRepository.deleteAll();
        this.userDeviceRepository.deleteAll();
        this.userRepository.deleteAll();
        user = addUser(UserRole.REGISTERED);
        admin = addUser(UserRole.ADMIN);
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void addProtocol() throws Exception {
        var occupation = createOccupation();
        var protocol = protocolRepository.save(new ProtocolEntity(occupation, ActionType.CREATE, user));

        checkProtocol(protocol, occupation);
    }

    @Test
    @WithMockUser(username = "ADMIN")
    public void getSince() throws Exception {
        var now = new Date().getTime() - 1000;
        protocolRepository.save(new ProtocolEntity(createOccupation(), ActionType.CREATE, admin));
        performGet("/rest/protocol/" + now)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", Matchers.hasSize(1)));
        now += 5000;
        performGet("/rest/protocol/" + now)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", Matchers.hasSize(0)));
    }

    @Test
    @WithMockUser(username = "REGISTERED")
    public void getSinceNotAdmin() throws Exception {
        performGet("/rest/protocol/1").andExpect(status().isUnauthorized());
    }

    private OccupationEntity createOccupation() {
        OccupationEntity occupation = new OccupationEntity();
        occupation.setId(new Random().nextLong());
        occupation.setCourt(1);
        occupation.setLastCourt(1);
        occupation.setDate(LocalDate.now());
        occupation.setDuration(4);
        occupation.setStart(LocalTime.now().plus(Duration.ofMinutes(30)));
        occupation.setSystemConfigId(1);
        occupation.setText("junit protocol test");
        occupation.setType(3);
        return occupation;
    }
}
