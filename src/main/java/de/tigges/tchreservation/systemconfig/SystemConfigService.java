package de.tigges.tchreservation.systemconfig;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.tigges.tchreservation.exception.FoundException;
import de.tigges.tchreservation.exception.NotFoundException;
import de.tigges.tchreservation.protocol.ActionType;
import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.protocol.jpa.ProtocolEntity;
import de.tigges.tchreservation.protocol.jpa.ProtocolRepository;
import de.tigges.tchreservation.reservation.model.ReservationSystemConfig;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigEntity;
import de.tigges.tchreservation.systemconfig.jpa.SystemConfigRepository;
import de.tigges.tchreservation.user.UserUtils;
import de.tigges.tchreservation.user.jpa.UserEntity;
import de.tigges.tchreservation.user.model.UserRole;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/systemconfig")
@RequiredArgsConstructor
public class SystemConfigService {

	private final SystemConfigRepository repository;
	private final SystemConfigValidator validator;
	private final ProtocolRepository protocolRepository;
	private final UserUtils userUtils;

	@GetMapping("/getone/{id}")
	Optional<ReservationSystemConfig> getOne(@PathVariable Long id) {
		return repository.findById(id).map(SystemConfigMapper::map);
	}

	@GetMapping("")
	public List<ReservationSystemConfig> getAll() {
		return StreamSupport.stream(repository.findAll().spliterator(), false).map(SystemConfigMapper::map)
				.collect(Collectors.toList());
	}

	@PostMapping("")
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ReservationSystemConfig add(@RequestBody ReservationSystemConfig config) {
		UserEntity loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
		repository.findById(config.getId()).ifPresent(e -> {
			throw new FoundException(EntityType.SYSTEM_CONFIGURATION, config.getId());
		});
		validator.validate(config, loggedInUser);
		SystemConfigEntity entity = repository.save(SystemConfigMapper.map(config));
		protocolRepository.save(new ProtocolEntity(entity, ActionType.CREATE, loggedInUser));
		return SystemConfigMapper.map(entity);
	}

	@PutMapping("")
	public @ResponseBody ReservationSystemConfig update(@RequestBody ReservationSystemConfig config) {
		UserEntity loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
		repository.findById(config.getId())
				.orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, config.getId()));
		validator.validate(config, loggedInUser);
		SystemConfigEntity entity = SystemConfigMapper.map(config);
		SystemConfigEntity savedEntity = repository.save(SystemConfigMapper.map(config));
		protocolRepository.save(new ProtocolEntity(savedEntity, entity, loggedInUser));
		return SystemConfigMapper.map(savedEntity);
	}

	@DeleteMapping("/{id}")
	public @ResponseBody ReservationSystemConfig delete(@PathVariable long id) {
		UserEntity loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
		SystemConfigEntity entity = repository.findById(id)
				.orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id));
		protocolRepository.save(new ProtocolEntity(entity, ActionType.DELETE, loggedInUser));
		repository.delete(entity);
		return SystemConfigMapper.map(entity);
	}
}
