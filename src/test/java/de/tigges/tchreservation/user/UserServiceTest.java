package de.tigges.tchreservation.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.hamcrest.Matchers;
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

import de.tigges.tchreservation.ProtocolTest;
import de.tigges.tchreservation.TchReservationApplication;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.user.jpa.UserDeviceEntity;
import de.tigges.tchreservation.user.jpa.UserDeviceRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TchReservationApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
public class UserServiceTest extends ProtocolTest {

	@Autowired
	private UserDeviceRepository userDeviceRepository;
	private UserEntity adminUser;
	private UserEntity registeredUser;

	@BeforeEach
	public void setup() throws Exception {
		this.protocolRepository.deleteAll();
		this.userDeviceRepository.deleteAll();
		this.userRepository.deleteAll();
		adminUser = addUser(UserRole.ADMIN);
		registeredUser = addUser(UserRole.REGISTERED);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addUser() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		checkUser(performPost("/rest/user/", user).andExpect(status().isOk()), user, false, ActionType.CREATE);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addUserNull() throws Exception {
		performPost("/rest/user/", "").andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addUserWithoutEmail() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		user.setEmail(null);
		performPost("/rest/user/", user).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addUserWithDevices() throws Exception {
		User user = createUser(0, UserRole.ADMIN, ActivationStatus.CREATED);
		for (int i = 0; i < 5; i++) {
			user.getDevices().add(createDevice(UserMapper.map(user), i, ActivationStatus.CREATED));
		}

		checkUser(performPost("/rest/user/", user).andExpect(status().isOk()), user, false, ActionType.CREATE);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addDevice() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));

		performPost("/rest/user/device", createDevice(user, 0, ActivationStatus.CREATED)).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void setStatus() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		performPut("/rest/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isOk());
		user.setStatus(ActivationStatus.VERIFIED_BY_USER);
		user.setPassword(null); // don't check password
		checkProtocol(user, ActionType.MODIFY);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void setStatusAllCombinations() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.CREATED));

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

	private void changeStatus(UserEntity user, ActivationStatus status, boolean expectOk) throws Exception {
		ResultActions actions = performPut("/rest/user/setStatus/" + user.getId() + "/" + status.toString());
		if (expectOk) {
			actions.andExpect(status().isOk());
		} else {
			actions.andExpect(status().isBadRequest());
		}
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addUserWithoutAuthorization() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		performPost("/rest/user/", user).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void addUserDuplicate() throws Exception {
		User user = createUser(0, UserRole.REGISTERED, ActivationStatus.ACTIVE);
		performPost("/rest/user/", user).andExpect(status().isOk());
		performPost("/rest/user/", user).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addUserDeviceWithoutAuthorization() throws Exception {
		UserDevice device = UserDeviceMapper
				.map(userDeviceRepository.save(createDeviceEntity(adminUser, 0, ActivationStatus.CREATED)));
		device.setUser(UserMapper.map(adminUser));
		System.out.println(device.getUser());
		performPost("/rest/user/device", device).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void addUserOwnDevice() throws Exception {
		UserDevice device = UserDeviceMapper
				.map(userDeviceRepository.save(createDeviceEntity(registeredUser, 0, ActivationStatus.CREATED)));
		performPost("/rest/user/device", device).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void setStatusWithUserNotFound() throws Exception {
		performPut("/rest/user/setStatus/666/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void setStatusWithoutAuthorization() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		performPut("/rest/user/setStatus/" + user.getId() + "/" + ActivationStatus.VERIFIED_BY_USER.toString())
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void update() throws Exception {
		UserEntity user = userRepository
				.save(new UserEntity("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));
		User modifiedUser = new User("modifiedEmail", "modifiedName", "modifiedPassword", UserRole.KIOSK,
				ActivationStatus.VERIFIED_BY_USER);
		modifiedUser.setId(user.getId());

		performPut("/rest/user/", modifiedUser).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateWithoutPassword() throws Exception {
		UserEntity user = userRepository
				.save(new UserEntity("email", "name", "password", UserRole.REGISTERED, ActivationStatus.CREATED));
		User modifiedUser = new User("modifiedEmail", "modifiedName", null, UserRole.KIOSK,
				ActivationStatus.VERIFIED_BY_USER);
		modifiedUser.setId(user.getId());

		performPut("/rest/user/", modifiedUser).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void updateNotMe() throws Exception {
		performPut("/rest/user/", adminUser).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void updateMe() throws Exception {
		registeredUser.setName("new name");
		performPut("/rest/user/", registeredUser).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void updateMyRoleNotAuthorized() throws Exception {
		registeredUser.setRole(UserRole.ADMIN);
		performPut("/rest/user/", registeredUser).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void updateMyStatusNotAuthorized() throws Exception {
		registeredUser.setStatus(ActivationStatus.LOCKED);
		performPut("/rest/user/", registeredUser).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void updateUserNotFound() throws Exception {
		registeredUser.setId(666);
		performPut("/rest/user", registeredUser).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteUser() throws Exception {
		performDelete("/rest/user/" + registeredUser.getId()).andExpect(status().isOk());
		performGet("/rest/user/" + registeredUser.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(ActivationStatus.REMOVED.toString()));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getUser() throws Exception {
		List<UserEntity> userList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			userList.add(userRepository.save(new UserEntity("email " + i, "name " + i, "password", UserRole.REGISTERED,
					ActivationStatus.ACTIVE)));
		}
		for (int i = 0; i < userList.size(); i++) {
			checkUser(performGet("/rest/user/" + userList.get(i).getId()).andExpect(status().isOk()), userList.get(i));
		}
	}

	@Test
	public void getUserWithoutAuthorization() throws Exception {
		performGet("/rest/user/" + adminUser.getId()).andExpect(status().is3xxRedirection());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getUserMe() throws Exception {
		performGet("/rest/user/" + registeredUser.getId()).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getUserNotMe() throws Exception {
		performGet("/rest/user/" + adminUser.getId()).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getDevices() throws Exception {
		List<UserDevice> devices = new ArrayList<>();
		UserEntity user = createUserEntity(0, UserRole.REGISTERED, ActivationStatus.CREATED);
		for (int i = 0; i < 15; i++) {
			if (i % 3 == 0) {
				user = userRepository.save(createUserEntity(i, UserRole.REGISTERED, ActivationStatus.CREATED));
			}
			devices.add(UserDeviceMapper
					.map(userDeviceRepository.save(createDeviceEntity(user, i, ActivationStatus.CREATED))));
		}

		for (UserDevice device : devices) {
			checkDevice(device);
		}
	}

	@Test
	public void getLoggedInUserAnonymous() throws Exception {
		checkUser(performGet("/rest/user/me").andExpect(status().isOk()), UserUtils.anonymous());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getLoggedInUserRegistered() throws Exception {
		checkUser(performGet("/rest/user/me").andExpect(status().isOk()), registeredUser);
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getUserByDevice() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.CREATED));
		UserDeviceEntity device0 = userDeviceRepository.save(createDeviceEntity(user, 0, ActivationStatus.CREATED));
		UserDeviceEntity device1 = userDeviceRepository.save(createDeviceEntity(user, 1, ActivationStatus.CREATED));

		User u = UserMapper.map(user);
		u.getDevices().add(UserDeviceMapper.map(device0));
		u.getDevices().add(UserDeviceMapper.map(device1));

		checkUser(performGet("/rest/user/getByDevice/" + device0.getId()).andExpect(status().isOk()), u, false, null);

		checkUser(performGet("/rest/user/getByDevice/" + device1.getId()).andExpect(status().isOk()), u, false, null);
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getUserByDeviceNotAuthorized() throws Exception {
		performGet("/rest/user/getByDevice/1").andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getUserByDeviceNotFound() throws Exception {
		performGet("/rest/user/getByDevice/666").andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getUserByName() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		Optional<UserEntity> foundUser = userRepository.findByNameOrEmail(user.getName(), "");
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getId()).isEqualTo(user.getId());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getUserByEMail() throws Exception {
		UserEntity user = userRepository.save(createUserEntity(0, UserRole.REGISTERED, ActivationStatus.ACTIVE));
		Optional<UserEntity> foundUser = userRepository.findByNameOrEmail("", user.getEmail());
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getId()).isEqualTo(user.getId());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getAllUser() throws Exception {
		int users = 50;
		for (int i = 0; i < users; i++) {
			userRepository.save(createRandomUser());
		}
		performGet("/rest/user/all").andExpect(status().isOk()).andExpect(jsonPath("$.*", Matchers.hasSize(users + 2)));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getAllUserWithoutAuthentication() throws Exception {
		performGet("/rest/user/all").andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void getDevice() throws Exception {
		checkDevice(UserDeviceMapper
				.map(userDeviceRepository.save(createDeviceEntity(registeredUser, 0, ActivationStatus.CREATED))));
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void getDeviceNotAuthorized() throws Exception {
		performGet("/rest/user/device/0").andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteDevice() throws Exception {
		UserDeviceEntity device = userDeviceRepository
				.save(createDeviceEntity(registeredUser, 0, ActivationStatus.CREATED));
		performDelete("/rest/user/device/" + device.getId()).andExpect(status().isOk());
		performGet("/rest/user/device/" + device.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(ActivationStatus.REMOVED.toString()));
	}

	@Test
	@WithMockUser(username = "ADMIN")
	public void deleteDeviceUnknown() throws Exception {
		UserDevice device = createDevice(registeredUser, 0, ActivationStatus.CREATED);
		performDelete("/rest/user/device/" + device.getId()).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "REGISTERED")
	public void deleteDeviceNotAuthorized() throws Exception {
		performDelete("/rest/user/device/0").andExpect(status().isUnauthorized());
	}

	private void checkDevice(UserDevice device) throws Exception {
		performGet("/rest/user/device/" + device.getId()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.deviceId").value(device.getDeviceId()))
				.andExpect(jsonPath("$.publicKey").value(device.getPublicKey())).andExpect(jsonPath("$.user").exists())
				.andExpect(jsonPath("$.user.id").value(device.getUser().getId()));
	}

	private UserEntity createRandomUser() {
		int i = new Random().nextInt(100000);
		return new UserEntity("email " + i, "name_" + i, "password_" + i, UserRole.values()[i % 5],
				ActivationStatus.values()[i % 5]);
	}

	private ResultActions checkUser(ResultActions resultActions, UserEntity user) throws Exception {
		return checkUser(resultActions, UserMapper.map(user), false, null);
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
			checkProtocol(UserMapper.map(createdUser), actionType);
		} else if (ActionType.MODIFY.equals(actionType) || ActionType.DELETE.equals(actionType)) {
			checkProtocol(UserMapper.map(user), actionType);
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

	private UserEntity createUserEntity(int i, UserRole role, ActivationStatus status) {
		return new UserEntity("myEmail " + i, "myName " + i, "mypass" + i, role, status);
	}

	private UserDevice createDevice(UserEntity user, int i, ActivationStatus status) {
		return new UserDevice(UserMapper.map(user), "deviceId " + i, status, "publicKey " + i);
	}

	private UserDeviceEntity createDeviceEntity(UserEntity user, int i, ActivationStatus status) {
		return new UserDeviceEntity(user, "deviceId " + i, status, "publicKey " + i);
	}
}
