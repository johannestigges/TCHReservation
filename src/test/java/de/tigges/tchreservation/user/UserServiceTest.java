package de.tigges.tchreservation.user;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class UserServiceTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;
	
//	@Autowired
//	UserService userService;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.userRepository.deleteAll();
	}

	@Test
	public void testSave() throws Exception {
		userRepository.deleteAll();

		mockMvc.perform(post("/user/add")
				.content(
						json(new User("test@user.org", "user", "secret", UserRole.REGISTERED, ActivationStatus.ACTIVE)))
				.contentType(contentType))
		.andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(jsonPath("$.id", is(notNullValue())))
		;
	}
	
	@Test
	public void testStatus() throws Exception {
		userRepository.deleteAll();
		User user = userRepository.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));
		
	mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString())).andExpect(status().isOk());
	mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString())).andExpect(status().isOk());
	mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.LOCKED.toString())).andExpect(status().isOk());
	mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString())).andExpect(status().isOk());
	mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.REMOVED.toString())).andExpect(status().isOk());
		
	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

}
