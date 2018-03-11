package de.tigges.tchreservation.user;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.tigges.tchreservation.user.model.User;
import de.tigges.tchreservation.user.model.UserDevice;

@Service
public class UserService {

	private UserRepository userRepository;
	private UserDeviceRepository userDeviceRepository;

	public UserService(UserRepository userRepository, UserDeviceRepository userDeviceRepository) {
		this.userRepository = userRepository;
		this.userDeviceRepository = userDeviceRepository;
	}

	@RequestMapping(path = "/findUser/:userId", method = RequestMethod.GET)
	public @ResponseBody Optional<User> findUserById(Long userId) {
		return userRepository.findById(userId);
	}

	@RequestMapping(path = "/findDevice/:deviceId", method = RequestMethod.GET)
	public @ResponseBody Optional<UserDevice> findDeviceById(Long deviceId) {
		return userDeviceRepository.findById(deviceId);
	}

	@RequestMapping(path = "/saveUser", method = RequestMethod.POST)
	public @ResponseBody User save(User user) {
		user.getDevices().forEach(d -> save(d));
		return userRepository.save(user);
	}

	@RequestMapping(path = "/saveUserDevice", method = RequestMethod.POST)
	public @ResponseBody UserDevice save(UserDevice userDevice) {
		return userDeviceRepository.save(userDevice);
	}

	@RequestMapping(path = "/hasUser/:userId", method = RequestMethod.GET)
	public @ResponseBody boolean hasUser(Long userId) {
		return findUserById(userId).isPresent();
	}
}
