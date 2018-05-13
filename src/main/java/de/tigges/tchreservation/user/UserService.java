package de.tigges.tchreservation.user;

import java.util.Optional;

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

import de.tigges.tchreservation.EntityType;
import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.Protocol;
import de.tigges.tchreservation.protocol.ProtocolRepository;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;

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
		return getLoggedInUser();
	}

	@GetMapping("/getAll")
	public @ResponseBody Iterable<User> getAll() {
		return userRepository.findAll();
	}

	@GetMapping("/get/{userId}")
	public @ResponseBody Optional<User> get(@PathVariable Long userId) {
		return userRepository.findById(userId);
	}

	@GetMapping("/getByDevice/{deviceId}")
	public @ResponseBody User getByDevice(@PathVariable Long deviceId) {
		UserDevice device = userDeviceRepository.findById(deviceId)
				.orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, deviceId));
		User user = userRepository.findById(device.getUser().getId())
				.orElseThrow(() -> new NotFoundException(EntityType.USER, device.getUser().getId()));
		user.getDevices().add(device);
		return user;
	}

	@GetMapping("/getDevice/{id}")
	public @ResponseBody Optional<UserDevice> findDeviceById(@PathVariable Long id) {
		return userDeviceRepository.findById(id);
	}

	@PostMapping("/add")
	public @ResponseBody User add(@RequestBody User user) {
		User loggedInUser = getLoggedInUser();
		checkUser(user);
		String cryptedPassword = encoder.encode(user.getPassword());
		user.setPassword(cryptedPassword);
		User savedUser = userRepository.save(user);
		protocolRepository.save(new Protocol(savedUser, ActionType.CREATE, loggedInUser));
		user.getDevices().forEach(device -> {
			device.setUser(savedUser);
			UserDevice savedDevice = userDeviceRepository.save(device);
			protocolRepository.save(new Protocol(savedDevice, ActionType.CREATE, loggedInUser));
			savedUser.getDevices().add(savedDevice);
		});
		return savedUser;
	}

	@PostMapping("/addDevice")
	public @ResponseBody UserDevice add(@RequestBody UserDevice userDevice) {
		UserDevice savedDevice = userDeviceRepository.save(userDevice);
		protocolRepository.save(new Protocol(savedDevice, ActionType.CREATE, getLoggedInUser()));
		return savedDevice;
	}

	@PutMapping("/setStatus/{userId}/{status}")
	public @ResponseBody void setStatus(@PathVariable long userId, @PathVariable ActivationStatus status) {
		User user = get(userId).orElseThrow(() -> new NotFoundException(EntityType.USER, userId));
		user.setStatus(status);
		userRepository.save(user);
		protocolRepository.save(new Protocol(user, ActionType.MODIFY, getLoggedInUser()));
	}

	@PutMapping("/setDeviceStatus/{id}/{status}")
	public @ResponseBody void setDeviceStatus(@PathVariable long id, @PathVariable ActivationStatus status) {
		UserDevice device = userDeviceRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, id));
		device.setStatus(status);
		userDeviceRepository.save(device);
		protocolRepository.save(new Protocol(device, ActionType.MODIFY, getLoggedInUser()));
	}

	@PutMapping("/")
	public @ResponseBody void update(@RequestBody User user) {
		get(user.getId()).orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));
		userRepository.save(user);
		protocolRepository.save(new Protocol(user, ActionType.MODIFY, getLoggedInUser()));
	}

	@DeleteMapping("/{userId}")
	public @ResponseBody void delete(@PathVariable long userId) {
		setStatus(userId, ActivationStatus.REMOVED);
	}

	@DeleteMapping("/device/{id}")
	public @ResponseBody void deleteDevice(@PathVariable long id) {
		setDeviceStatus(id, ActivationStatus.REMOVED);
	}

	private void checkUser(User user) {
		if (user == null) {
			throw new BadRequestException("no user");
		}
		if (user.getEmail() == null) {
			throw new BadRequestException("no email");
		}
		Optional<User> dbUser = userRepository.findByNameOrEmail(user.getName(), user.getEmail());
		if (dbUser.isPresent() && dbUser.get().getId() != user.getId()) {
			throw new BadRequestException(String.format("user with name '%s' and/or email '%s' already exists.",
					user.getName(), user.getEmail()));
		}
	}
}
