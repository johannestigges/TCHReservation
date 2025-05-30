package de.tigges.tchreservation.user;

import de.tigges.tchreservation.exception.ErrorCode;
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
import de.tigges.tchreservation.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/rest/user")
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final ProtocolRepository protocolRepository;
    private final LoggedinUserService loggedinUserService;
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final Validator validator;

    @GetMapping("/me")
    public User getMyUser() {
        return UserMapper.map(loggedinUserService.getLoggedInUser());
    }

    @GetMapping("/all")
    public @ResponseBody Iterable<User> getAll() {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        Iterable<UserEntity> allUsers = userRepository.findAllByOrderByName();
        return StreamSupport.stream(allUsers.spliterator(), false)
                .map(UserMapper::map)
                .toList();
    }

    @GetMapping("/{userId}")
    public @ResponseBody Optional<User> get(@PathVariable Long userId) {
        loggedinUserService.verifyHasRoleOrSelf(userId, UserRole.ADMIN);
        return userRepository.findById(userId)
                .map(UserMapper::map)
                .map(this::addDevices);
    }

    @GetMapping("/getByDevice/{deviceId}")
    public @ResponseBody User getByDevice(@PathVariable Long deviceId) {
        var device = getDevice(deviceId);
        var userId = device.getUser().getId();
        loggedinUserService.verifyHasRoleOrSelf(userId, UserRole.ADMIN);
        var user = userRepository.findById(userId).map(UserMapper::map)
                .orElseThrow(() -> new NotFoundException(EntityType.USER, userId));
        return addDevices(user);
    }

    @GetMapping("/device/{deviceId}")
    public @ResponseBody UserDevice getDevice(@PathVariable Long deviceId) {
        loggedinUserService.verifyHasRole(UserRole.ADMIN);
        return userDeviceRepository.findById(deviceId).map(UserDeviceMapper::map)
                .orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, deviceId));
    }

    @PostMapping("")
    public @ResponseBody User add(@RequestBody User user) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        checkNewUser(user);
        var cryptedPassword = encoder.encode(user.getPassword());
        user.setPassword(cryptedPassword);
        var savedUserEntity = userRepository.save(UserMapper.map(user));
        var savedUser = UserMapper.map(savedUserEntity);
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
        var loggedInUser = loggedinUserService.verifyHasRoleOrSelf(userDevice.getUser().getId(), UserRole.ADMIN);
        var savedDevice = userDeviceRepository.save(UserDeviceMapper.map(userDevice));
        protocolRepository.save(new ProtocolEntity(savedDevice, ActionType.CREATE, loggedInUser));
        return UserDeviceMapper.map(savedDevice);
    }

    @PutMapping("/setStatus/{userId}/{status}")
    public @ResponseBody void setStatus(@PathVariable long userId, @PathVariable ActivationStatus status) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        var dbUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(EntityType.USER, userId));
        var saveUser = new UserEntity(dbUser);
        saveUser.setStatus(status);
        userRepository.save(saveUser);
        protocolRepository.save(new ProtocolEntity(saveUser, dbUser, loggedInUser));
    }

    @PutMapping("/device/setStatus/{deviceId}/{status}")
    public @ResponseBody void setDeviceStatus(@PathVariable long deviceId, @PathVariable ActivationStatus status) {
        var loggedInUser = loggedinUserService.verifyHasRole(UserRole.ADMIN);
        var device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException(EntityType.USER_DEVICE, deviceId));
        device.setStatus(status);
        userDeviceRepository.save(device);
        protocolRepository.save(new ProtocolEntity(device, ActionType.MODIFY, loggedInUser));
    }

    @PutMapping("")
    public @ResponseBody void update(@RequestBody User user) {
        var loggedInUser = loggedinUserService.verifyHasRoleOrSelf(user.getId(), UserRole.ADMIN);
        checkUser(user);
        var dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException(EntityType.USER, user.getId()));

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(dbUser.getPassword());
        } else {
            user.setPassword(encoder.encode(user.getPassword()));
        }

        if (!UserUtils.hasRole(loggedInUser, UserRole.ADMIN)) {
            if (!user.getName().equals(dbUser.getName())) {
                throw validator.authorizationException(ErrorCode.USER_CANNOT_MODIFY_NAME);
            }
            if (user.getRole() != dbUser.getRole()) {
                throw validator.authorizationException(ErrorCode.USER_CANNOT_MODIFY_ROLE);
            }
            if (user.getStatus() != dbUser.getStatus()) {
                throw validator.authorizationException(ErrorCode.USER_CANNOT_MODIFY_STATUS);
            }
        }

        var userEntity = UserMapper.map(user);
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

    private User addDevices(User user) {
        userDeviceRepository.findByUserId(user.getId())
                .forEach(d -> user.getDevices().add(UserDeviceMapper.map(d)));
        return user;
    }

    private void checkUser(User user) {
        if (isEmpty(user.getName())) {
            throw validator.badRequestException(ErrorCode.USER_NAME_EMPTY);
        }
    }

    private void checkNewUser(User user) {
        checkUser(user);
        if (isEmpty(user.getPassword())) {
            throw validator.badRequestException(ErrorCode.PASSWORD_EMPTY);
        }
        var email = user.getEmail();
        if (isEmpty(email)) {
            email = "INVALID EMAIL IGNORE IN SEARCH";
        }
        var dbUser = userRepository.findByNameOrEmail(user.getName(), email);
        if (dbUser.isPresent() && !Objects.equals(dbUser.get().getId(), user.getId())) {
            throw validator.badRequestException(ErrorCode.USER_EXISTS,
                    user.getName(), user.getEmail());
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
