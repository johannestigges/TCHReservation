package de.tigges.tchreservation.user;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TchReservationApplication.class)
@WebAppConfiguration
public class UserServiceTest extends ProtocolTest {

	@Autowired
	private UserDeviceRepository userDeviceRepository;
	private User adminUser;
	private User registeredUser;

	@Before
	public void setup() throws Exception {
		this.protocolRepository.deleteAll();
		this.userDeviceRepository.deleteAll();
		this.userRepository.deleteAll();
		adminUser = addUser(UserRole.ADMIN);
		registeredUser = addUser(UserRole.REGISTERED);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testAddUser() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		checkUser(performPost("/user/add", user).andExpect(status().isOk()), user, true, ActionType.CREATE);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testAddUserWithDevices() throws Exception {
		User user = createUser(0, UserRole.ADMIN, ActivationStatus.CREATED);
		for (int i = 0; i < 5; i++) {
			user.getDevices().add(createDevice(user, i, ActivationStatus.CREATED));
		}

		checkUser(performPost("/user/add", user).andExpect(status().isOk()), user, true, ActionType.CREATE);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testAddDevice() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));

		performPost("/user/addDevice", createDevice(user, 0, ActivationStatus.CREATED)).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testSetStatus() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		performPut("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isOk());
		user.setStatus(ActivationStatus.VERIFIED_BY_USER);
		user.setPassword(null); // don't check password
		checkProtocol(user, ActionType.MODIFY);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testSetStatusAllCombinations() throws Exception {
		User user = userRepository
				.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));

		// check from CREATED
		changeStatus(user, ActivationStatus.ACTIVE, false);
		changeStatus(user, ActivationStatus.LOCKED, false);
		changeStatus(user, ActivationStatus.REMOVED, false);
		changeStatus(user, ActivationStatus.VERIFIED_BY_USER, true);

		// check from VERIFIED_BY_USER
		changeStatus(user, ActivationStatus.CREATED, false);
		changeStatus(user, ActivationStatus.LOCKED, false);
		changeStatus(user, ActivationStatus.REMOVED, false);
		changeStatus(user, ActivationStatus.ACTIVE, true);

		// check from ACTIVE
		changeStatus(user, ActivationStatus.CREATED, false);
		changeStatus(user, ActivationStatus.VERIFIED_BY_USER, false);
		changeStatus(user, ActivationStatus.REMOVED, true);
		changeStatus(user, ActivationStatus.ACTIVE, true);
		changeStatus(user, ActivationStatus.LOCKED, true);

		// check from LOCKED
		changeStatus(user, ActivationStatus.CREATED, false);
		changeStatus(user, ActivationStatus.VERIFIED_BY_USER, false);
		changeStatus(user, ActivationStatus.REMOVED, true);
		changeStatus(user, ActivationStatus.ACTIVE, true);
		changeStatus(user, ActivationStatus.REMOVED, true);

		// check from REMOVED
		changeStatus(user, ActivationStatus.CREATED, false);
		changeStatus(user, ActivationStatus.VERIFIED_BY_USER, false);
		changeStatus(user, ActivationStatus.LOCKED, false);
		changeStatus(user, ActivationStatus.ACTIVE, true);
	}

	private void changeStatus(User user, ActivationStatus status, boolean expectOk) throws Exception {
		ResultActions actions = performPut("/user/setStatus/" + user.getId() + "/" + status.toString());
		if (expectOk) {
			actions.andExpect(status().isOk());
		} else {
			actions.andExpect(status().isBadRequest());
		}
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testAddUserWithoutAuthorization() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		performPost("/user/add", user).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testSetStatusWithUserNotFound() throws Exception {
		performPut("/user/setStatus/666/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testSetStatusWithoutAuthorization() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		performPut("/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testUpdate() throws Exception {
		User user = userRepository
				.save(new User("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));
		User modifiedUser = new User("modifiedEmail", "modifiedName", "modifiedPassword", UserRole.KIOSK,
				ActivationStatus.VERIFIED_BY_USER);
		modifiedUser.setId(user.getId());

		performPut("/user/", modifiedUser).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testUpdateNotMe() throws Exception {
		performPut("/user/", adminUser).andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithMockUser(username = "REGISTERED")
	public void testUpdateMe() throws Exception {
		registeredUser.setName("new name");
		performPut("/user/", registeredUser).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testUpdateMyRoleNotAuthorized() throws Exception {
		registeredUser.setRole(UserRole.ADMIN);
		performPut("/user/", registeredUser).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testUpdateMyStatusNotAuthorized() throws Exception {
		registeredUser.setStatus(ActivationStatus.LOCKED);
		performPut("/user/", registeredUser).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testUpdateUserNotFound() throws Exception {
		registeredUser.setId(666);
		performPut("/user/", registeredUser).andExpect(status().isNotFound());
	}
	

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGet() throws Exception {
		List<User> userList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			userList.add(userRepository.save(
					new User("email " + i, "name " + i, "password", UserRole.REGISTERED, ActivationStatus.ACTIVE)));
		}
		for (int i = 0; i < userList.size(); i++) {
			checkUser(performGet("/user/get/" + userList.get(i).getId()).andExpect(status().isOk()), userList.get(i),
					false, null);
		}
	}

	@Test
	public void testGetUserWithoutAuthorization() throws Exception {
		performGet("/user/get/" + adminUser.getId()).andExpect(status().is3xxRedirection());
	}
	
	@Test
	@WithMockUser(username = "REGISTERED")
	public void testGetUserMe() throws Exception {
		performGet("/user/get/" + registeredUser.getId()).andExpect(status().isOk());
	}
	@Test
	@WithMockUser(username = "REGISTERED")
	public void testGetUserNotMe() throws Exception {
		performGet("/user/get/" + adminUser.getId()).andExpect(status().isUnauthorized());
	}
	
	
	@Test
	@WithMockUser(username = "ADMIN")
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
	public void testGetLoggedInUserAnonymous() throws Exception {
		checkUser(performGet("/user/me").andExpect(status().isOk()), User.anonymous(), false, null);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGetLoggedInUserAdmin() throws Exception {
		checkUser(performGet("/user/me").andExpect(status().isOk()), adminUser, false, null);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGetByDevice() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		UserDevice device0 = userDeviceRepository.save(createDevice(user, 0, ActivationStatus.CREATED));
		UserDevice device1 = userDeviceRepository.save(createDevice(user, 1, ActivationStatus.CREATED));

		user.getDevices().add(device0);
		checkUser(performGet("/user/getByDevice/" + device0.getId()).andExpect(status().isOk()), user, false, null);

		user.getDevices().clear();
		user.getDevices().add(device1);
		checkUser(performGet("/user/getByDevice/" + device1.getId()).andExpect(status().isOk()), user, false, null);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGetByName() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		User foundUser = userRepository.findByNameOrEmail(user.getName(), "")
				.orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));
		assertThat(foundUser.getId(), Matchers.is(user.getId()));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGetByEMail() throws Exception {
		User user = userRepository.save(createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		User foundUser = userRepository.findByNameOrEmail("", user.getEmail())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));
		assertThat(foundUser.getId(), Matchers.is(user.getId()));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void testGetAll() throws Exception {
		int users = 50;
		for (int i = 0; i < users; i++) {
			userRepository.save(createRandomUser());
		}
		performGet("/user/getAll").andExpect(status().isOk()).andExpect(jsonPath("$.*", Matchers.hasSize(users + 2)));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void testGetAllWithoutAuthentication() throws Exception {
		performGet("/user/getAll").andExpect(status().isUnauthorized());
	}

	private void checkDevice(UserDevice device) {
		try {
			performGet("/user/getDevice/" + device.getId()) //
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
		resultActions.andExpect(jsonPath("$.id").isNotEmpty()).andExpect(jsonPath("$.email").value(user.getEmail()))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.role").value(user.getRole().name()))

				.andExpect(jsonPath("$.status").value(user.getStatus().name()))
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
