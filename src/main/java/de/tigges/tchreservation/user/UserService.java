package de.tigges.tchreservation.user;

import java.util.Optional;

import org.springframework.stereotype.Service;

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

	public Optional<User> findUserById (Long userId) {
		return userRepository.findById(userId);
	}
	
	public Optional<UserDevice> findDeviceById (Long deviceId) {
		return userDeviceRepository.findById(deviceId);
	}
	
	public User save(User user) {
		user.getDevices().forEach(d -> save(d));
		return userRepository.save(user);
	}
	
	public UserDevice save(UserDevice userDevice) {
		return userDeviceRepository.save(userDevice);
	}
	
	public boolean hasUser(Long userId) {
		return findUserById(userId).isPresent();
	}
}
