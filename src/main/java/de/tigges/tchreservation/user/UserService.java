package de.tigges.tchreservation.user;

import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.user.model.ActivationStatus;
import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;

@RestController
@RequestMapping("/user")
public class UserService {

	private UserRepository userRepository;
	private UserDeviceRepository userDeviceRepository;

	public UserService(UserRepository userRepository, UserDeviceRepository userDeviceRepository) {
		this.userRepository = userRepository;
		this.userDeviceRepository = userDeviceRepository;
	}

	@RequestMapping(path = "/get/{userId}", method = RequestMethod.GET)
	public @ResponseBody User get(@PathVariable Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
	}

	@RequestMapping(path = "/getDevice/{deviceId}", method = RequestMethod.GET)
	public @ResponseBody Optional<UserDevice> findDeviceById(@PathVariable Long deviceId) {
		return userDeviceRepository.findById(deviceId);
	}

	@RequestMapping(path = "/add", method = RequestMethod.POST)
	public @ResponseBody User add(User user) {
		user.getDevices().forEach(d -> add(d));
		return userRepository.save(user);
	}

	@RequestMapping(path = "/addDevice", method = RequestMethod.POST)
	public @ResponseBody UserDevice add(UserDevice userDevice) {
		return userDeviceRepository.save(userDevice);
	}

	@RequestMapping(path = "/setStatus/{userId}/{status}", method = RequestMethod.GET)
	public @ResponseBody void setStatus(@PathVariable long userId, @PathVariable ActivationStatus status) {
		User user = get(userId);
		user.setStatus(status);
		userRepository.save(user);
	}
}
