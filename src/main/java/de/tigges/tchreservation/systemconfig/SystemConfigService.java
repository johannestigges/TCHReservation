package de.tigges.tchreservation.systemconfig;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import de.tigges.tchreservation.reservation.model.SystemConfigReservationType;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeMapper;
import de.tigges.tchreservation.systemconfig.jpa.ReservationTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

    private final SystemConfigRepository systemConfigRepository;
    private final ReservationTypeRepository reservationTypeRepository;
    private final SystemConfigValidator validator;
    private final ProtocolRepository protocolRepository;
    private final UserUtils userUtils;

    @GetMapping("/getone/{id}")
    Optional<ReservationSystemConfig> getOne(@PathVariable Long id) {
        return systemConfigRepository.findById(id)
                .map(SystemConfigMapper::map)
                .map(this::setTypes);
    }

    @GetMapping("/getall")
    public List<ReservationSystemConfig> getAll() {
        return StreamSupport.stream(systemConfigRepository.findAll().spliterator(), false)
                .map(SystemConfigMapper::map)
                .map(this::setTypes)
                .toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public @ResponseBody ReservationSystemConfig add(@RequestBody ReservationSystemConfig config) {
        var loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
        systemConfigRepository.findById(config.getId()).ifPresent(e -> {
            throw new FoundException(EntityType.SYSTEM_CONFIGURATION, config.getId());
        });
        validator.validate(config, loggedInUser);
        var entity = systemConfigRepository.save(SystemConfigMapper.map(config));
        protocolRepository.save(new ProtocolEntity(entity, ActionType.CREATE, loggedInUser));
        insertTypes(loggedInUser, config.getTypes(),entity);
        return SystemConfigMapper.map(entity);
    }

    @PutMapping("")
    @Transactional
    public @ResponseBody ReservationSystemConfig update(@RequestBody ReservationSystemConfig config) {
        var loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
        systemConfigRepository.findById(config.getId())
                .orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, config.getId()));
        validator.validate(config, loggedInUser);
        var entity = SystemConfigMapper.map(config);
        var savedEntity = systemConfigRepository.save(SystemConfigMapper.map(config));
        protocolRepository.save(new ProtocolEntity(savedEntity, entity, loggedInUser));
        reservationTypeRepository.deleteBySystemConfigId(config.getId());
        insertTypes(loggedInUser, config.getTypes(), savedEntity);
        return SystemConfigMapper.map(savedEntity);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public @ResponseBody ReservationSystemConfig delete(@PathVariable long id) {
        var loggedInUser = userUtils.verifyHasRole(UserRole.ADMIN);
        var entity = systemConfigRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(EntityType.SYSTEM_CONFIGURATION, id));
        reservationTypeRepository.deleteBySystemConfigId(id);
        protocolRepository.save(new ProtocolEntity(entity, ActionType.DELETE, loggedInUser));
        systemConfigRepository.delete(entity);
        return SystemConfigMapper.map(entity);
    }

    private ReservationSystemConfig setTypes(ReservationSystemConfig systemConfig) {
        systemConfig.setTypes(getTypes(systemConfig.getId()));
        return systemConfig;
    }

    private List<SystemConfigReservationType> getTypes(long systemConfigId) {
        var entities = reservationTypeRepository.findBySystemConfigId(systemConfigId);
        return StreamSupport.stream(entities.spliterator(), false) //
                .map(ReservationTypeMapper::map)
                .toList();
    }

    private void insertTypes(UserEntity loggedInUser, Collection<SystemConfigReservationType> types, SystemConfigEntity systemConfig) {
        if (types == null) {
            return;
        }
        types.forEach(type -> {
            var entity = ReservationTypeMapper.map(type);
            entity.setSystemConfig(systemConfig);
            reservationTypeRepository.save(entity);
            protocolRepository.save(new ProtocolEntity(entity, ActionType.CREATE, loggedInUser));
        });
    }
}
