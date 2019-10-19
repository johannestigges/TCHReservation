package de.tigges.tchreservation.user;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.exception.AuthorizationException;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.user.jpa.UserDeviceEntity;
import de.tigges.tchreservation.user.jpa.UserDeviceRepository;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.jpa.UserRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;
import de.tigges.tchreservation.user.model.UserRole;

@RestController
@RequestMapping("/user")
public class UserService extends UserAwareService {

	private UserDeviceRepository userDeviceRepository;
	private ProtocolRepository protocolRepository;
	private PasswordEncoder encoder;

	public UserService(UserRepository userRepository, UserDeviceRepository userDeviceRepository,
			ProtocolRepository protocolRepository) {
		super(userRepository);
		this.userDeviceRepository = userDeviceRepository;
		this.protocolRepository = protocolRepository;
		encoder = new BCryptPasswordEncoder();
	}

	@GetMapping("/me")
	public User getMyUser() {
		return UserMapper.map(getLoggedInUser());
	}

	@GetMapping("/all")
	public @ResponseBody Iterable<User> getAll() {
		if (!isAdmin(getLoggedInUser()))
			throw new AuthorizationException("not authorized");

		Iterable<UserEntity> allUsers = userRepository.findAll();
		return StreamSupport.stream(allUsers.spliterator(), false).map(UserMapper::map).collect(Collectors.toList());
	}

	@GetMapping("/{userId}")
	public @ResponseBody Optional<User> get(@PathVariable Long userId) {
		return find(userId, getLoggedInUser()).map(UserMapper::map).map(this::addDevices);
	}

	@GetMapping("/getByDevice/{deviceId}")
	public @ResponseBody User getByDevice(@PathVariable Long deviceId) {
		UserDevice device = getDevice(deviceId);
		long userId = device.getUser().getId();
		User user = userRepository.findById(userId).map(UserMapper::map)
				.orElseThrow(() -> new NotFoundException(EntityType.USER, userId));
		return addDevices(user);
	}

	@GetMapping("/device/{deviceId}")
	public @ResponseBody UserDevice getDevice(@PathVariable Long deviceId) {
		if (!isAdmin(getLoggedInUser()))
			throw new AuthorizationException("not authorized");
		return userDeviceRepository.findById(deviceId).map(UserDeviceMapper::map)
				.orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, deviceId));
	}

	@PostMapping("/")
	public @ResponseBody User add(@RequestBody User user) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser))
			throw new AuthorizationException("not authorized");
		checkUser(user);
		String cryptedPassword = encoder.encode(user.getPassword());
		user.setPassword(cryptedPassword);
		UserEntity savedUserEntity = userRepository.save(UserMapper.map(user));
		User savedUser = UserMapper.map(savedUserEntity);
		protocolRepository.save(new ProtocolEntity(savedUserEntity, ActionType.CREATE, loggedInUser));
		user.getDevices().forEach(device -> {
			device.setUser(savedUser);
			UserDeviceEntity savedDevice = userDeviceRepository.save(UserDeviceMapper.map(device));
			protocolRepository.save(new ProtocolEntity(savedDevice, ActionType.CREATE, loggedInUser));
			savedUserEntity.getDevices().add(savedDevice);
		});
		return addDevices(savedUser);
	}

	@PostMapping("/device")
	public @ResponseBody UserDevice add(@RequestBody UserDevice userDevice) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser) && !is(loggedInUser, userDevice.getUser().getId()))
			throw new AuthorizationException("not authorized");
		UserDeviceEntity savedDevice = userDeviceRepository.save(UserDeviceMapper.map(userDevice));
		protocolRepository.save(new ProtocolEntity(savedDevice, ActionType.CREATE, loggedInUser));
		return UserDeviceMapper.map(savedDevice);
	}

	@PutMapping("/setStatus/{userId}/{status}")
	public @ResponseBody void setStatus(@PathVariable long userId, @PathVariable ActivationStatus status) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser))
			throw new AuthorizationException("not authorized");
		UserEntity dbUser = find(userId, getLoggedInUser())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, userId));
		UserEntity saveUser = new UserEntity(dbUser);
		saveUser.setStatus(status);
		userRepository.save(saveUser);
		protocolRepository.save(new ProtocolEntity(saveUser, dbUser, loggedInUser));
	}

	@PutMapping("/device/setStatus/{deviceId}/{status}")
	public @ResponseBody void setDeviceStatus(@PathVariable long deviceId, @PathVariable ActivationStatus status) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser))
			throw new AuthorizationException("not authorized");

		UserDeviceEntity device = userDeviceRepository.findById(deviceId)
				.orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, deviceId));
		device.setStatus(status);
		userDeviceRepository.save(device);
		protocolRepository.save(new ProtocolEntity(device, ActionType.MODIFY, loggedInUser));
	}

	@PutMapping("/")
	public @ResponseBody void update(@RequestBody User user) {
		UserEntity loggedInUser = getLoggedInUser();
		if (!isAdmin(loggedInUser) && !is(loggedInUser, user.getId()))
			throw new AuthorizationException("not authorized");

		UserEntity dbUser = userRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));

		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			user.setPassword(dbUser.getPassword());
		} else {
			user.setPassword(encoder.encode(user.getPassword()));
		}

		if (!isAdmin(loggedInUser)) {
			if (user.getRole() != dbUser.getRole()) {
				throw new AuthorizationException("user cannot modify role.");
			}
			if (user.getStatus() != dbUser.getStatus()) {
				throw new AuthorizationException("user cannot modify status.");
			}
		}

		UserEntity userEntity = UserMapper.map(user);
		userRepository.save(userEntity);
		protocolRepository.save(new ProtocolEntity(userEntity, dbUser, loggedInUser));
	}

	@DeleteMapping("/{userId}")
	public void delete(@PathVariable long userId) {
		setStatus(userId, ActivationStatus.REMOVED);
	}

	@DeleteMapping("/device/{deviceId}")
	public void deleteDevice(@PathVariable long deviceId) {
		setDeviceStatus(deviceId, ActivationStatus.REMOVED);
	}

	private Optional<UserEntity> find(Long userId, UserEntity loggedInUser) {
		if (!isAdmin(loggedInUser) && !is(loggedInUser, userId))
			throw new AuthorizationException("not authorized");
		return userRepository.findById(userId);

	}

	private User addDevices(User user) {
		userDeviceRepository.findByUserId(user.getId()).forEach(d -> user.getDevices().add(UserDeviceMapper.map(d)));
		return user;
	}

	private void checkUser(User user) {
		if (user.getEmail() == null) {
			throw new BadRequestException("no email");
		}
		Optional<UserEntity> dbUser = userRepository.findByNameOrEmail(user.getName(), user.getEmail());
		if (dbUser.isPresent() && dbUser.get().getId() != user.getId()) {
			throw new BadRequestException(String.format("user with name '%s' and/or email '%s' already exists.",
					user.getName(), user.getEmail()));
		}
	}

	private boolean isAdmin(UserEntity user) {
		return UserUtils.hasRole(user.getRole(), UserRole.ADMIN) && ActivationStatus.ACTIVE.equals(user.getStatus());
	}

	private boolean is(UserEntity user, long userId) {
		return user.getId() == userId;
	}
}
