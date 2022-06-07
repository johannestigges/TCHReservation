package de.tigges.tchreservation.applicationproperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tigges.tchreservation.ServiceTest;
import de.tigges.tchreservation.TchReservationApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
class ApplicationPropertiesServiceTest extends ServiceTest {

	@Test
	void test() throws Exception {

		String contentAsString = performGet("/rest/application/properties") //
				.andExpect(status().is2xxSuccessful()) //
				.andReturn().getResponse().getContentAsString();

		ApplicationProperties properties = new ObjectMapper().readValue(contentAsString, ApplicationProperties.class);
		assertThat(properties.getTitle()).isEqualTo("junit title");
	}
}
