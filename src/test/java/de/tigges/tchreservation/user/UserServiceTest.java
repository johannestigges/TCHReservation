package de.tigges.tchreservation.user;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class UserServiceTest extends ProtocolTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDeviceRepository userDeviceRepository;

	@Autowired
	private ProtocolRepository protocolRepository;

	@Before
	public void setup() throws Exception {
		this.protocolRepository.deleteAll();
		this.userDeviceRepository.deleteAll();
		this.userRepository.deleteAll();
	}

	@Test
	public void testAddUser() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		checkUser(mockMvc.perform(post("/user/add").content(json(user)).contentType(contentType))
				.andExpect(status().isOk()), user, true, ActionType.CREATE);
	}

	@Test
	public void testAddUserWithDevices() throws Exception {
		User user = createUser(0, UserRole.ADMIN, ActivationStatus.CREATED);
		for (int i = 0; i < 5; i++) {
			user.getDevices().add(createDevice(user, i, ActivationStatus.CREATED));
		}

		checkUser(mockMvc.perform(post("/user/add").content(json(user)).contentType(contentType))//
				.andExpect(status().isOk()), user, true, ActionType.CREATE);
	}

	@Test
	public void testAddDevice() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));

		mockMvc.perform(post("/user/addDevice").content(json(createDevice(user, 0, ActivationStatus.CREATED)))
				.contentType(contentType)).andExpect(status().isOk());
	}

	@Test
	public void testSetStatus() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString()))
				.andExpect(status().isOk());
		user.setStatus(ActivationStatus.VERIFIED_BY_USER);
		checkProtocol(user, ActionType.MODIFY);
	}

	@Test
	public void testStatus() throws Exception {
		User user = userRepository
				.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));

		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.LOCKED.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.ACTIVE.toString()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/user/setStatus/" + user.getId() + "/" + ActivationStatus.REMOVED.toString()))
				.andExpect(status().isOk());
	}

	@Test
	public void testUpdate() throws Exception {
		User user = userRepository
				.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));
		User modifiedUser = new User("modifiedEmail", "modifiedName", "modifiedPassword", UserRole.KIOSK,
				ActivationStatus.VERIFIED_BY_USER);
		modifiedUser.setId(user.getId());

		checkUser(mockMvc.perform(put("/user/update").contentType(contentType).content(json(modifiedUser)))
				.andExpect(status().isOk()), modifiedUser, false, null);

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
					userList.get(i), false, null);
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
		checkUser(mockMvc.perform(get("/user/getByDevice/" + device0.getId())).andExpect(status().isOk()), user, false,
				null);

		user.getDevices().clear();
		user.getDevices().add(device1);
		checkUser(mockMvc.perform(get("/user/getByDevice/" + device1.getId())).andExpect(status().isOk()), user, false,
				null);
	}

	@Test
	public void testGetByName() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		User foundUser = userRepository.findByNameOrEmail(user.getName(), "")
				.orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));
		assertThat(foundUser.getId(), Matchers.is(user.getId()));
	}

	@Test
	public void testGetByEMail() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		User foundUser = userRepository.findByNameOrEmail("", user.getEmail())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));
		assertThat(foundUser.getId(), Matchers.is(user.getId()));
	}

	@Test
	public void testGetAll() throws Exception {
		int users = 50;
		for (int i = 0; i < users; i++) {
			userRepository.save(createRandomUser());
		}
		mockMvc.perform(get("/user/getAll")).andExpect(status().isOk())
				.andExpect(jsonPath("$.*", Matchers.hasSize(users)));
		;
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

	private User createRandomUser() {
		int i = new Random().nextInt(100000);
		return new User("email " + i, "name_" + i, "password_" + i, UserRole.values()[i % 5],
				ActivationStatus.values()[i % 5]);
	}

	private ResultActions checkUser(ResultActions resultActions, User user, boolean passwordEncoded,
			ActionType actionType) throws Exception {
		resultActions.andExpect(content().contentType(contentType)).andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.email").value(user.getEmail()))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.role").value(user.getRole().ordinal()))

				.andExpect(jsonPath("$.status").value(user.getStatus().toString()))
		//
		;
		// if (!passwordEncoded) {
		// resultActions.andExpect(jsonPath("$.password").value(user.getPassword()));
		// }

		if (ActionType.CREATE.equals(actionType)) {
			User createdUser = getResponseJson(resultActions, User.class);
			resultActions.andExpect(jsonPath("$.id").value(createdUser.getId()));
			// assertThat(new BCryptPasswordEncoder().matches(user.getPassword(),
			// createdUser.getPassword()),
			// Matchers.is(true));
			checkProtocol(createdUser, actionType);
		} else if (ActionType.MODIFY.equals(actionType) || ActionType.DELETE.equals(actionType)) {
			checkProtocol(user, actionType);
		}

		checkDevices(resultActions, user);
		return resultActions;
	}

	private ResultActions checkDevices(ResultActions resultActions, User user) throws Exception {
		if (user.getDevices().isEmpty()) {
			resultActions.andExpect(jsonPath("$.devices").isEmpty());
		} else {
			resultActions.andExpect(jsonPath("$.devices").isArray())
					.andExpect(jsonPath("$.devices", Matchers.hasSize(user.getDevices().size())));
		}
		return resultActions;
	}

	private User createUser(int i, UserRole role, ActivationStatus status) {
		return new User("myEmail " + i, "myName " + i, "mypass" + i, role, status);
	}

	private UserDevice createDevice(User user, int i, ActivationStatus status) {
		return new UserDevice(user, "deviceId " + i, status, "publicKey " + i);
	}
}
