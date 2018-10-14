package de.tigges.tchreservation.protocol;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.reservation.model.Occupation;
import de.tigges.tchreservation.reservation.model.ReservationType;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class ProtocolServiceTest extends ProtocolTest {

	private User user;

	@Before
	public void setup() throws Exception {

		this.protocolRepository.deleteAll();
		this.userRepository.deleteAll();
		user = addUser(UserRole.REGISTERED, ActivationStatus.ACTIVE);
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addProtocol() throws Exception {
		Occupation occupation = createOccupation();
		Protocol protocol = protocolRepository.save(new Protocol(occupation, ActionType.CREATE, user));
		
		checkProtocol(protocol, occupation);
	}
	
	private Occupation createOccupation() {
		Occupation occupation = new Occupation();
		occupation.setId(new Random().nextLong());
		occupation.setCourt(1);
		occupation.setLastCourt(1);
		occupation.setDate(LocalDate.now());
		occupation.setDuration(4);
		occupation.setStart(LocalTime.now().plus(Duration.ofMinutes(30)));
		occupation.setSystemConfigId(1);
		occupation.setText("junit protocol test");
		occupation.setType(ReservationType.TOURNAMENT);
		return occupation;
	}
	

}
