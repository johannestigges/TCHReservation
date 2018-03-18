package de.tigges.tchreservation.user;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)

@WebAppConfiguration
public class UserServiceTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;

	private HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDeviceRepository userDeviceRepository;

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

		this.userDeviceRepository.deleteAll();
		this.userRepository.deleteAll();
	}

	@Test
	public void testSave() throws Exception {

		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		checkUser(mockMvc.perform(post("/user/add").content(json(user)).contentType(contentType))
				.andExpect(status().isOk()), user);
	}

	@Test
	public void testSaveUserWithDevices() throws Exception {
		User user = createUser(0, UserRole.ADMIN, ActivationStatus.CREATED);
		for (int i = 0; i < 5; i++) {
			user.getDevices().add(createDevice(user, i, ActivationStatus.CREATED));
		}

		checkUser(mockMvc.perform(post("/user/add").content(json(user)).contentType(contentType))//
				.andExpect(status().isOk()), user);
	}

	@Test
	public void testSaveDevice() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		
		mockMvc.perform(post("/user/addDevice").content(json(createDevice(user, 0, ActivationStatus.CREATED)))
				.contentType(contentType)).andExpect(status().isOk());
	}

	@Test
	public void testStatus() throws Exception {
		User user = userRepository
				.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));

		mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.LOCKED.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(get("/user/setStatus/" + user.getId() + "/" + ActivationStatus.REMOVED.toString()))
				.andExpect(status().isOk());
	}

	@Test
	public void testGet() throws Exception {
		List<User> userList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			userList.add(userRepository.save(
					new User("email " + i, "name " + i, "password", UserRole.REGISTERED, ActivationStatus.ACTIVE)));
		}
		for (int i = 0; i < userList.size(); i++) {
			checkUser(mockMvc.perform(get("/user/get/" + userList.get(i).getId())).andExpect(status().isOk()),
					userList.get(i));
		}
	}

	@Test
	public void testGetDevices() throws Exception {
		List<UserDevice> devices = new ArrayList<>();

		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED);
		for (int i = 0; i < 15; i++) {
			if (i % 3 == 0) {
				user = userRepository.save(createUser(i, UserRole.REGISTERED, ActivationStatus.CREATED));
			}
			devices.add(userDeviceRepository.save(createDevice(user, i, ActivationStatus.CREATED)));
		}

		devices.forEach(device -> checkDevice(device));
	}
	
	@Test
	public void testGetByDevice() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		UserDevice device0 = userDeviceRepository.save(createDevice(user, 0, ActivationStatus.CREATED));
		UserDevice device1 = userDeviceRepository.save(createDevice(user, 1, ActivationStatus.CREATED));
		
		user.getDevices().add(device0);
		checkUser(mockMvc.perform(get("/user/getByDevice/" + device0.getId())).andExpect(status().isOk()),user);
		
		user.getDevices().clear();
		user.getDevices().add(device1);
		checkUser(mockMvc.perform(get("/user/getByDevice/" + device1.getId())).andExpect(status().isOk()),user);
	}

	private void checkDevice(UserDevice device) {
		try {
			mockMvc.perform(get("/user/getDevice/" + device.getId())) //
					.andExpect(status().isOk()) //
					.andExpect(jsonPath("$.deviceId").value(device.getDeviceId()))
					.andExpect(jsonPath("$.publicKey").value(device.getPublicKey()))
					.andExpect(jsonPath("$.user").exists())
					.andExpect(jsonPath("$.user.id").value(device.getUser().getId()));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private ResultActions checkUser(ResultActions resultActions, User user) throws Exception {
		resultActions.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.email").value(user.getEmail()))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.password").value(user.getPassword()))
				.andExpect(jsonPath("$.role").value(user.getRole().toString()))
				.andExpect(jsonPath("$.status").value(user.getStatus().toString()))
		//
		;
		checkDevices(resultActions, user);
		return resultActions;
	}

	private ResultActions checkDevices(ResultActions resultActions, User user) throws Exception {
		if (user.getDevices().isEmpty()) {
			resultActions.andExpect(jsonPath("$.devices").isEmpty());
		} else {
			resultActions.andExpect(jsonPath("$.devices").isArray())
			.andExpect(jsonPath("$.devices", Matchers.hasSize(user.getDevices().size())))
			
			;
			
		}

		return resultActions;
	}

	private User createUser(int i, UserRole role, ActivationStatus status) {
		return new User("myEmail " + i, "myName " + i, "myPassword " + i, role, status);
	}

	private UserDevice createDevice(User user, int i, ActivationStatus status) {
		return new UserDevice(user, "deviceId " + i, status, "publicKey " + i);
	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

	@SuppressWarnings("unchecked")
	protected <T> T jsonObject(byte[] content, Class<T> c) throws HttpMessageNotReadableException, IOException {
		MockHttpInputMessage mockHttpInputMessage = new MockHttpInputMessage(content);
		return (T) this.mappingJackson2HttpMessageConverter.read(c, mockHttpInputMessage);
	}

}
