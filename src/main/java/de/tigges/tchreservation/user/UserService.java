package de.tigges.tchreservation.user;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.exception.BadRequestException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;

@RestController
@RequestMapping("/user")
public class UserService {

	private UserRepository userRepository;
	private UserDeviceRepository userDeviceRepository;
	private PasswordEncoder encoder;

	public UserService(UserRepository userRepository, UserDeviceRepository userDeviceRepository) {
		this.userRepository = userRepository;
		this.userDeviceRepository = userDeviceRepository;
		encoder = new BCryptPasswordEncoder();
	}

	@RequestMapping(path = "/get/{userId}", method = RequestMethod.GET)
	public @ResponseBody Optional<User> get(@PathVariable Long userId) {
		return userRepository.findById(userId);
	}

	@RequestMapping(path = "/getByDevice/{deviceId}", method = RequestMethod.GET)
	public @ResponseBody User getByDevice(@PathVariable Long deviceId) {
		UserDevice device = userDeviceRepository.findById(deviceId)
				.orElseThrow(() -> new NotFoundException("user device", deviceId));
		User user = userRepository.findById(device.getUser().getId())
				.orElseThrow(() -> new NotFoundException("user", device.getUser().getId()));
		user.getDevices().add(device);
		return user;
	}

	@RequestMapping(path = "/getDevice/{id}", method = RequestMethod.GET)
	public @ResponseBody Optional<UserDevice> findDeviceById(@PathVariable Long id) {
		return userDeviceRepository.findById(id);
	}

	@RequestMapping(path = "/add", method = RequestMethod.POST)
	public @ResponseBody User add(@RequestBody User user) {
		checkUser(user);
		String cryptedPassword = encoder.encode(user.getPassword());
		System.out.println(String.format("encrypt %s = %s",user.getPassword(),cryptedPassword));
		user.setPassword(cryptedPassword);
		User savedUser = userRepository.save(user);
		user.getDevices().forEach(device -> {
			device.setUser(savedUser);
			savedUser.getDevices().add(userDeviceRepository.save(device));
		});
		return savedUser;
	}

	@RequestMapping(path = "/addDevice", method = RequestMethod.POST)
	public @ResponseBody UserDevice add(@RequestBody UserDevice userDevice) {
		return userDeviceRepository.save(userDevice);
	}

	@RequestMapping(path = "/setStatus/{userId}/{status}", method = RequestMethod.GET)
	public @ResponseBody void setStatus(@PathVariable long userId, @PathVariable ActivationStatus status) {
		User user = get(userId).orElseThrow(() -> new NotFoundException("user", userId));
		user.setStatus(status);
		userRepository.save(user);
	}

	private void checkUser(User user) {
		if (user == null) {
			throw new BadRequestException("no user");
		}
		if (user.getEmail() == null) {
			throw new BadRequestException("no email");
		}
	}
}
